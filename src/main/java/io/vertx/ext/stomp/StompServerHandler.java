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

package io.vertx.ext.stomp;

import io.vertx.ext.stomp.frame.Frame;
import io.vertx.core.Future;

/**
 *
 * Created by Navid Mitchell on 2019-01-25.
 */
public interface StompServerHandler {

    Future<Void> authenticate(String user, String password);

    void send(Frame frame);

    void subscribe(Frame frame);

    void unsubscribe(Frame frame);

    void begin(Frame frame);

    void abort(Frame frame);

    void commit(Frame frame);

    void ack(Frame frame);

    void nack(Frame frame);

    void disconnected();

}
