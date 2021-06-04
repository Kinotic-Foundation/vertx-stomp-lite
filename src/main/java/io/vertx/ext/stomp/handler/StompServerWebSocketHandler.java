/*
 *  Copyright (c) 2011-2015 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.stomp.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.stomp.StompServerHandlerFactory;
import io.vertx.ext.stomp.StompServerOptions;
import io.vertx.ext.stomp.frame.FrameParser;
import io.vertx.ext.stomp.frame.InvalidConnectFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by Navid Mitchell on 2019-02-04.
 */
public class StompServerWebSocketHandler implements Handler<ServerWebSocket> {

    private static final Logger log = LoggerFactory.getLogger(StompServerWebSocketHandler.class);

    private Vertx vertx;
    private StompServerOptions options;
    private StompServerHandlerFactory factory;

    public StompServerWebSocketHandler(Vertx vertx,
                                       StompServerOptions options,
                                       StompServerHandlerFactory factory) {
        this.vertx = vertx;
        this.options = options;
        this.factory = factory;
    }

    @Override
    public void handle(ServerWebSocket socket) {
        DefaultStompServerConnection defaultStompServerConnection = new DefaultStompServerConnection(socket,
                                                                                                     vertx,
                                                                                                     options,
                                                                                                     factory);
        if (!socket.path().equals(options.getWebsocketPath())) {
            String error = "Receiving a web socket connection on an invalid path (" + socket.path() + "), the path is "
                             + "configured to " + options.getWebsocketPath() + ". Rejecting connection";
            log.error(error);

            socket.reject();

            defaultStompServerConnection.clientCausedException(new IllegalStateException(error), false);
        }else{

            socket.exceptionHandler((exception) -> {
                log.debug("The STOMP server caught a WebSocket error - closing connection", exception);
                defaultStompServerConnection.clientCausedException(exception, false);
            });

            socket.closeHandler( v -> defaultStompServerConnection.close());

            FrameParser parser = new FrameParser(options);
            parser.errorHandler(exception -> defaultStompServerConnection.clientCausedException(exception, false))
                  .handler(defaultStompServerConnection);

            socket.handler(buffer -> {
                // Additional check to make sure that we don't parse a bunch of data when the client has not successfully authenticated
                if(!defaultStompServerConnection.isConnected()) {
                    // client has not connected yet make ensure the client is sending a connect frame without parsing it completely
                    if(buffer.length() > options.getMaxConnectFrameLength()){

                        log.debug("Client sent a frame larger than the maximum allowed connect frame");
                        // frame is incomplete
                        defaultStompServerConnection.clientCausedException(
                                new InvalidConnectFrame("Client sent a frame larger than the maximum allowed connect frame", buffer), false);

                    }else if (buffer.length() > 7){

                        String possibleConnectCommand = new String(buffer.getBytes(0, 7));
                        if(possibleConnectCommand.equals("CONNECT")){
                            // initial frame is within required parameters
                            parser.handle(buffer);
                        }else{
                            log.debug("Initial frame does not contain a connect command");
                            // frame is incorrect
                            defaultStompServerConnection.clientCausedException(
                                    new InvalidConnectFrame("Initial frame does not contain a connect command", buffer), false);
                        }

                    }else{
                        log.debug("Client sent an incomplete connect frame");
                        // frame is incomplete
                        defaultStompServerConnection.clientCausedException(
                                new InvalidConnectFrame("Client sent an incomplete connect frame", buffer), false);
                    }
                }else{
                    parser.handle(buffer);
                }
            });
        }
    }
}
