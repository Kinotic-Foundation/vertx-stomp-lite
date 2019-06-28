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

package examples;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.stomp.StompServerConnection;
import io.vertx.ext.stomp.StompServerHandler;
import io.vertx.ext.stomp.frame.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by Navid Mitchell on 2019-02-05.
 */
public class DefaultStompServerHandler implements StompServerHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultStompServerHandler.class);

    private final StompServerConnection connection;
    private final Vertx vertx;
    private final EventBus eventBus;



    public DefaultStompServerHandler(StompServerConnection connection,
                                     Vertx vertx) {
        this.connection = connection;
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
    }

    @Override
    public Future<Void> authenticate(String user, String password) {
        return Future.succeededFuture();
    }

    @Override
    public void send(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void subscribe(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void unsubscribe(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void begin(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void abort(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void commit(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void ack(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void nack(Frame frame) {
        log.debug("Frame received\n" + frame.toString());
    }

    @Override
    public void disconnected() {
        log.debug("Client disconnected");
    }

}