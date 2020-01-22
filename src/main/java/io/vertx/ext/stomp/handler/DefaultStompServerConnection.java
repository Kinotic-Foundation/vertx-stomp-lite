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
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.stomp.StompServerConnection;
import io.vertx.ext.stomp.StompServerHandler;
import io.vertx.ext.stomp.StompServerHandlerFactory;
import io.vertx.ext.stomp.StompServerOptions;
import io.vertx.ext.stomp.frame.Frame;
import io.vertx.ext.stomp.frame.FrameParser;
import io.vertx.ext.stomp.frame.Frames;
import io.vertx.ext.stomp.frame.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by Navid Mitchell on 2019-01-10.
 */
class DefaultStompServerConnection implements Handler<Frame>, StompServerConnection {

    private static final Logger log = LoggerFactory.getLogger(DefaultStompServerConnection.class);

    private final ServerWebSocket serverWebSocket;
    private final Vertx vertx;
    private final StompServerOptions options;
    private final StompServerHandler stompServerHandler;

    private boolean connected = false;
    private boolean closed = false;
    private volatile long lastClientActivity;
    private volatile long lastServerActivity;
    private long serverHeartbeat = -1;
    private long clientHeartbeat = -1;


    DefaultStompServerConnection(ServerWebSocket serverWebSocket,
                                 Vertx vertx,
                                 StompServerOptions options,
                                 StompServerHandlerFactory factory) {
        this.serverWebSocket = serverWebSocket;
        this.vertx = vertx;
        this.options = options;

        // Create new handler to do the bulk of the work..
        this.stompServerHandler = factory.create(this);

        if(log.isDebugEnabled()){
            log.debug("New Stomp Connection "+serverWebSocket.remoteAddress().host());
        }
    }

    @Override
    public String binaryHandlerID() {
        return serverWebSocket.binaryHandlerID();
    }

    @Override
    public String textHandlerID() {
        return serverWebSocket.textHandlerID();
    }

    @Override
    public Promise<Void> write(Frame frame) {
        return write(frame.toBuffer(options.isTrailingLine()));
    }

    @Override
    public Promise<Void> write(Buffer buffer) {
        Promise<Void> ret = Promise.promise();
        onServerActivity();
        try{
            serverWebSocket.writeBinaryMessage(buffer, ret);
        } catch (Exception e) {
            ret.fail(e);
        }
        return ret;
    }

    @Override
    public Promise<Void> handleReceipt(Frame frame) {
        Promise<Void> ret = Promise.promise();
        String receipt = frame.getReceipt();
        if (receipt != null) {
            write(Frames.createReceiptFrame(receipt, Headers.create()))
                    .future()
                    .setHandler(ret);
        }else{
            ret.complete();
        }
        return ret;
    }

    @Override
    public Promise<Void> sendError(Throwable throwable) {
        return write(Frames.createErrorFrame(throwable, options.isDebugEnabled()));
    }

    @Override
    public Promise<Void> sendErrorAndDisconnect(Throwable throwable) {
        Promise<Void> ret = Promise.promise();
        sendError(throwable)
                .future()
                .setHandler(event -> {
                    // now that data was sent close connection and finish promise
                    close();
                    if (event.succeeded()){
                        ret.complete();
                    }else{
                        ret.fail(event.cause());
                    }
                });
        return ret;
    }

    /**
     * Used to signal that the processing of a frame resulted in an exception
     * @param t the exception that was caused
     * @param sendErrorFrame true if an ERROR frame should be sent to the client prior to closing it
     */
    public void clientCausedException(Throwable t, boolean sendErrorFrame){
        try {
            stompServerHandler.exception(t);
        } catch (Exception e) {
            log.error("StompServerHandler.exception handler threw an exception.. You should fix your handler not to throw exceptions.", e);
        }
        if(sendErrorFrame) {
            logIfFailed(sendErrorAndDisconnect(t),
                        "Problem sending ERROR frame to client");
        }else{
            close();
        }
    }

    // An example is where the client keeps trying to subscribe to unauthorized destination..
    public void close() {
        if(!closed) {
            if(log.isDebugEnabled()) {
                log.debug("Closing Stomp Connection "+serverWebSocket.remoteAddress().host());
            }

            connected = false;

            try {
                cancelHeartbeat();
            } catch (Exception e) {
                log.error("StompServerHandler unhandled error on cancelHeartbeat", e);
            }

            try {

                //*** This must be called under all circumstances so the Handler can clean up any client subscriptions ***
                stompServerHandler.disconnected();

            } catch (Exception e) {
                log.error("StompServerHandler.disconnected() handler threw an exception.. You should fix your handler not to throw exceptions.", e);
            }

            try {
                if(!serverWebSocket.isClosed()) {
                    serverWebSocket.close();
                }
            } catch (Exception e) {
                // Ignore it, the web socket has already been closed.
                log.warn("Error closing serverWebSocket.", e);
            }
            closed = true;
        }
    }


    /****                                                                                                       ****
     ****                                            Handler Logic                                              ****
     ****                                                                                                       ****/

    @Override
    public void handle(Frame frame) {
        if(!closed) {
            try {
                switch (frame.getCommand()) {
                    case CONNECT:
                        onConnect(frame);
                        break;
                    case SEND:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.send(frame);
                        break;
                    case SUBSCRIBE:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.subscribe(frame);
                        break;
                    case UNSUBSCRIBE:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.unsubscribe(frame);
                        break;
                    case BEGIN:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.begin(frame);
                        break;
                    case ABORT:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.abort(frame);
                        break;
                    case COMMIT:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.commit(frame);
                        break;
                    case ACK:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.ack(frame);
                        break;
                    case NACK:
                        ensureConnected();
                        onClientActivity();
                        stompServerHandler.nack(frame);
                        break;
                    case DISCONNECT:
                        ensureConnected();
                        onClientActivity();
                        handleReceipt(frame);
                        close();
                        break;
                    case PING:
                        ensureConnected();
                        onClientActivity(); // we just increment activity stomp pings do not expect a response
                        break;
                    default:
                        throw new IllegalStateException("Unknown command");
                }
            } catch (Exception e) {
                clientCausedException(e, true);
            }
        } else {
            log.error("THIS SHOULD NEVER HAPPEN!! Frame Handler called after close.");
        }
    }

    private void ensureConnected() {
        if (!connected) {
            throw new IllegalStateException("Client must provide a connect frame before any other frames");
        }
    }

    private void onConnect(Frame frame) {
        // Server negotiation
        List<String> accepted = new ArrayList<>();
        String accept = frame.getHeader(Frame.ACCEPT_VERSION);
        if (accept == null) {
            accepted.add("1.2");
        } else {
            accepted.addAll(Arrays.asList(accept.split(FrameParser.COMMA)));
        }

        String version = negotiate(accepted);
        if (version == null) {
            // Spec says: if the server and the client do not share any common protocol versions, then the server MUST respond with an error.
            throw new IllegalStateException("Client protocol requirement does not mach versions supported by the server.");
        }

        // Now authenticate client providing headers passed to CONNECT frame
        stompServerHandler.authenticate(frame.getHeaders())
                          .future()
                          .setHandler(authenticatePromise -> {

            if (authenticatePromise.succeeded()) {

                Headers headers = Headers.create(authenticatePromise.result());
                headers.add(Frame.VERSION, version); // Spec says: The server will respond back with the highest version of the protocol -> version
                headers.add(Frame.HEARTBEAT, Frame.Heartbeat.create(options.getHeartbeat()).toString());

                write(new Frame(Frame.Command.CONNECTED, headers, null))
                    .future()
                    .setHandler(writePromise -> {
                        if(writePromise.succeeded()){
                            // now that we are connected Compute heartbeat, and register serverHeartbeat and clientHeartbeat
                            Frame.Heartbeat clientHeartbeat = Frame.Heartbeat.parse(frame.getHeader(Frame.HEARTBEAT));
                            Frame.Heartbeat serverHeartbeat = Frame.Heartbeat.create(options.getHeartbeat());
                            long clientHeartbeatPeriod = Frame.Heartbeat.computeClientHeartbeatPeriod(clientHeartbeat, serverHeartbeat);
                            long serverHeartbeatPeriod = Frame.Heartbeat.computeServerHeartbeatPeriod(clientHeartbeat, serverHeartbeat);

                            onClientActivity();

                            configureHeartbeat(clientHeartbeatPeriod, serverHeartbeatPeriod);

                            if (log.isDebugEnabled()) {
                                log.debug("Stomp connected. Host: " + serverWebSocket.remoteAddress().host());
                            }

                            connected = true;
                        }else{
                            if(log.isDebugEnabled()){
                                log.debug("Could not send CONNECTED frame. Host: "+serverWebSocket.remoteAddress().host(), writePromise.cause());
                            }
                            close(); // cleanup
                        }
                    });

            } else {
                logIfFailed(sendErrorAndDisconnect(new IllegalStateException("Authentication Failed", authenticatePromise.cause())),
                            "Problem Sending Authentication Error to client");
            }
        });
    }

    private String negotiate(List<String> accepted) {
        List<String> supported = Collections.singletonList("1.2");
        for (String v : supported) {
            if (accepted.contains(v)) {
                return v;
            }
        }
        return null;
    }

    private void cancelHeartbeat() {
        if (serverHeartbeat >= 0) {
            vertx.cancelTimer(serverHeartbeat);
            serverHeartbeat = -1;
        }

        if (clientHeartbeat >= 0) {
            vertx.cancelTimer(clientHeartbeat);
            clientHeartbeat = -1;
        }
    }

    private void onClientActivity() {
        lastClientActivity = System.nanoTime();
    }

    private void onServerActivity() {
        lastServerActivity = System.nanoTime();
    }

    private void ping() {
        // we send directly so we do not increment serverActivity since we do not want pings to count towards that metric
        serverWebSocket.writeBinaryMessage(Buffer.buffer(FrameParser.EOL));
    }

    private void configureHeartbeat(long clientHeartbeatPeriod, long serverHeartbeatPeriod) {
        if (serverHeartbeatPeriod > 0) {
            serverHeartbeat = vertx.setPeriodic(serverHeartbeatPeriod, event -> {
                long delta = System.nanoTime() - lastServerActivity;
                final long deltaInMs = TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS);
                if (deltaInMs > serverHeartbeatPeriod) {
                    ping();
                }
            });
        }
        if (clientHeartbeatPeriod > 0) {
            clientHeartbeat = vertx.setPeriodic(clientHeartbeatPeriod, l -> {
                long delta = System.nanoTime() - lastClientActivity;
                final long deltaInMs = TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS);
                if (deltaInMs > clientHeartbeatPeriod * 2) {
                    if (log.isDebugEnabled()) {
                        log.debug("Disconnecting client " + this + " - no client activity in the last " + deltaInMs + " ms");
                    }
                    close();
                }
            });
        }
    }

    private void logIfFailed(Promise<Void> promise, String message){
        if(log.isDebugEnabled()){
            promise.future()
                   .setHandler(event -> {
                       if(event.failed()){
                           log.debug(message, event.cause());
                       }
                   });
        }
    }

}
