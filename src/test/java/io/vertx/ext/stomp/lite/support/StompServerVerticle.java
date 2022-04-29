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

package io.vertx.ext.stomp.lite.support;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.stomp.lite.StompServer;
import io.vertx.ext.stomp.lite.StompServerOptions;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * Created by Navid Mitchell on 2019-01-09.
 */
public class StompServerVerticle extends AbstractVerticle {

    private final String RESPONSE = "<!DOCTYPE html><html lang=en><meta charset=UTF-8><title>Stomp Server</title><style>@import url(https://fonts.googleapis.com/css?family=Share+Tech+Mono);body{background:#000}svg{width:100%;height:120px;background:#000}</style><body><svg height=100px version=1.1 xmlns=http://www.w3.org/2000/svg xmlns:xlink=http://www.w3.org/1999/xlink><style type=\"text/css\"><![CDATA[ text { filter: url(#filter); fill: white; font-family: 'Share Tech Mono', sans-serif; font-size: 100px; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale; } ]]> </style><defs><filter id=filter><feFlood flood-color=black result=black /><feFlood flood-color=red result=flood1 /><feFlood flood-color=limegreen result=flood2 /><feOffset dx=3 dy=0 in=SourceGraphic result=off1a /><feOffset dx=2 dy=0 in=SourceGraphic result=off1b /><feOffset dx=-3 dy=0 in=SourceGraphic result=off2a /><feOffset dx=-2 dy=0 in=SourceGraphic result=off2b /><feComposite in=flood1 in2=off1a operator=in result=comp1 /><feComposite in=flood2 in2=off2a operator=in result=comp2 /><feMerge result=merge1 width=100% x=0><feMergeNode in=black /><feMergeNode in=comp1 /><feMergeNode in=off1b /><animate attributeName=y dur=4s id=y keyTimes=\"0; 0.362; 0.368; 0.421; 0.440; 0.477; 0.518; 0.564; 0.593; 0.613; 0.644; 0.693; 0.721; 0.736; 0.772; 0.818; 0.844; 0.894; 0.925; 0.939; 1\"repeatCount=indefinite values=\"104px; 104px; 30px; 105px; 30px; 2px; 2px; 50px; 40px; 105px; 105px; 20px; 6ßpx; 40px; 104px; 40px; 70px; 10px; 30px; 104px; 102px\"/><animate attributeName=height dur=4s id=h keyTimes=\"0; 0.362; 0.368; 0.421; 0.440; 0.477; 0.518; 0.564; 0.593; 0.613; 0.644; 0.693; 0.721; 0.736; 0.772; 0.818; 0.844; 0.894; 0.925; 0.939; 1\"repeatCount=indefinite values=\"10px; 0px; 10px; 30px; 50px; 0px; 10px; 0px; 0px; 0px; 10px; 50px; 40px; 0px; 0px; 0px; 40px; 30px; 10px; 0px; 50px\"/></feMerge><feMerge result=merge2 width=100% x=0 height=65px y=60px><feMergeNode in=black /><feMergeNode in=comp2 /><feMergeNode in=off2b /><animate attributeName=y dur=4s id=y keyTimes=\"0; 0.055; 0.100; 0.125; 0.159; 0.182; 0.202; 0.236; 0.268; 0.326; 0.357; 0.400; 0.408; 0.461; 0.493; 0.513; 0.548; 0.577; 0.613; 1\"repeatCount=indefinite values=\"103px; 104px; 69px; 53px; 42px; 104px; 78px; 89px; 96px; 100px; 67px; 50px; 96px; 66px; 88px; 42px; 13px; 100px; 100px; 104px;\"/><animate attributeName=height dur=4s id=h keyTimes=\"0; 0.055; 0.100; 0.125; 0.159; 0.182; 0.202; 0.236; 0.268; 0.326; 0.357; 0.400; 0.408; 0.461; 0.493; 0.513;  1\"repeatCount=indefinite values=\"0px; 0px; 0px; 16px; 16px; 12px; 12px; 0px; 0px; 5px; 10px; 22px; 33px; 11px; 0px; 0px; 10px\"/></feMerge><feMerge><feMergeNode in=SourceGraphic /><feMergeNode in=merge1 /><feMergeNode in=merge2 /></feMerge></filter></defs><g><text x=0 y=100>Stomp Yo</text></g></svg></body></html>";

    private static final Logger log = LoggerFactory.getLogger(StompServerVerticle.class);

    private HttpServer httpServer;

    @Override
    public void start() {

        Router router = Router.router(vertx);
        router.route().handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.end(RESPONSE);
        });


        StompServerOptions properties = new StompServerOptions();

        DefaultStompServerHandlerFactory stompServerHandlerFactory = new DefaultStompServerHandlerFactory(vertx);

        httpServer = vertx.createHttpServer(new HttpServerOptions().setWebSocketSubProtocols(List.of("v12.stomp")))
                          .webSocketHandler(StompServer.createWebSocketHandler(vertx,
                                                                               properties,
                                                                               stompServerHandlerFactory))
                          .requestHandler(router)
                          .exceptionHandler(event -> log.error(
                                  "Stomp server Exception before completing Client Connection",
                                  event))
                          .listen(properties.getPort(), properties.getHost());

    }

    @Override
    public void stop() {
        httpServer.close();
    }
}

