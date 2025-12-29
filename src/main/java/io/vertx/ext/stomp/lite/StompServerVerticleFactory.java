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

import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.stomp.lite.handler.StompServerWebSocketHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * Created by Navid Mitchell on 2019-02-04.
 */
public class StompServerVerticleFactory {

    /**
     * Create a STOMP server verticle that will handle Stomp connections.
     * @param httpOptions the HTTP server options to use to create the underlying HTTP server
     * @param stompOptions the STOMP server options
     * @param factory the factory to create STOMP server handlers
     * @param router to use for handling Http requests (can be null)
     * @return the STOMP server verticle
     */
    public static StompServerVerticle createWebSocketHandler(HttpServerOptions httpOptions,
                                                             StompServerOptions stompOptions,
                                                             StompServerHandlerFactory factory,
                                                             Router router) {
        return new StompServerVerticle(httpOptions, stompOptions, factory, router);
    }

}
