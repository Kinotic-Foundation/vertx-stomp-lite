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

package io.vertx.ext.stomp.lite;

import io.vertx.core.Promise;
import io.vertx.ext.stomp.lite.frame.Frame;

import java.util.Map;

/**
 *
 * Created by Navid Mitchell on 2019-01-25.
 */
public interface StompServerHandler {

    /**
     * Requests authentication for the given credentials
     * @param connectHeaders all of the headers provided with the CONNECT frame. This will include the login and passcode headers.
     * @return a {@link Promise} completed normally to authenticate or failed to represent a failed authentication
     *         The promise must contain a Map that will provide any additional headers to be returned to the client with the CONNECTED frame
     */
    Promise<Map<String, String>> authenticate(Map<String, String> connectHeaders);

    void send(Frame frame);

    void subscribe(Frame frame);

    void unsubscribe(Frame frame);

    void begin(Frame frame);

    void abort(Frame frame);

    void commit(Frame frame);

    void ack(Frame frame);

    void nack(Frame frame);

    /**
     * This is called when the processing of a client request resulted in an exception.
     * Ex: parsing or handling of a STOMP frame resulted in an exception.
     * After this is called the client connection will automatically be closed.
     *
     * @param t the exception that occurred
     */
    void exception(Throwable t);

    /**
     * Called when a client explicitly sends a DISCONNECT frame.
     * This will be called before {@link StompServerHandler#closed()}.
     */
    void disconnected();

    /**
     * Called when the client connection is closed
     */
    void closed();

}
