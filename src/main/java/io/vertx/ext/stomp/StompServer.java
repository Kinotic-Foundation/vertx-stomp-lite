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

import io.vertx.ext.stomp.handler.StompServerWebSocketHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

/**
 *
 * Created by Navid Mitchell on 2019-02-04.
 */
public class StompServer {

    public static Handler<ServerWebSocket> createWebSocketHandler(Vertx vertx,
                                                                  StompServerOptions options,
                                                                  StompServerHandlerFactory factory){
        return new StompServerWebSocketHandler(vertx,options,factory);
    }

}
