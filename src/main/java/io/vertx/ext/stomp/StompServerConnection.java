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

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.stomp.frame.Frame;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

/**
 *
 * Created by Navid Mitchell on 2019-01-10.
 */
public interface StompServerConnection {

    /**
     * When a {@code Websocket} is created it automatically registers an event handler with the event bus - the ID of that
     * handler is given by this method.
     * <p>
     * Given this ID, a different event loop can send a binary frame to that event handler using the event bus and
     * that buffer will be received by this instance in its own event loop and written to the underlying connection. This
     * allows you to write data to other WebSockets which are owned by different event loops.
     *s
     * @return the binary handler id
     */
    String binaryHandlerID();

    /**
     * When a {@code Websocket} is created it automatically registers an event handler with the eventbus, the ID of that
     * handler is given by {@code textHandlerID}.
     * <p>
     * Given this ID, a different event loop can send a text frame to that event handler using the event bus and
     * that buffer will be received by this instance in its own event loop and written to the underlying connection. This
     * allows you to write data to other WebSockets which are owned by different event loops.
     */
    String textHandlerID();

    /**
     * @return the remote address for this socket
     */
    SocketAddress remoteAddress();

    /**
     * @return the local address for this socket
     */
    SocketAddress localAddress();

    /**
     * @return true if this {@link io.vertx.core.http.HttpConnection} is encrypted via SSL/TLS.
     */
    boolean isSsl();

    /**
     * @return SSLSession associated with the underlying socket. Returns null if connection is
     *         not SSL.
     * @see javax.net.ssl.SSLSession
     */
    SSLSession sslSession();

    /**
     * Note: Java SE 5+ recommends to use javax.net.ssl.SSLSession#getPeerCertificates() instead of
     * of javax.net.ssl.SSLSession#getPeerCertificateChain() which this method is based on. Use {@link #sslSession()} to
     * access that method.
     *
     * @return an ordered array of the peer certificates. Returns null if connection is
     *         not SSL.
     * @throws javax.net.ssl.SSLPeerUnverifiedException SSL peer's identity has not been verified.
     * @see javax.net.ssl.SSLSession#getPeerCertificateChain()
     * @see #sslSession()
     */
    X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException;

    /**
     * Writes the given frame to the socket.
     *
     * @param frame the frame, must not be {@code null}.
     * @return a {@link Promise} that will be completed when the data is successfully sent.
     *         Will be failed if there is a problem sending the data or the underlying TCP connection is already closed.
     */
    Promise<Void> write(Frame frame);

    /**
     * Writes the given buffer to the socket. This is a low level API that should be used carefully.
     *
     * @param buffer the buffer
     * @return a {@link Promise} that will be completed when the data is successfully sent.
     *         Will be failed if there is a problem sending the data or the underlying TCP connection is already closed.
     */
    Promise<Void> write(Buffer buffer);

    /**
     * Will send receipt frame acknowledgement to clients when requested by a receipt header
     * @param frame to check for a receipt header
     * @return a {@link Promise} that will be completed when the data is successfully sent.
     *         Will be failed if there is a problem sending the data or the underlying TCP connection is already closed.
     */
    Promise<Void> sendReceiptIfNeeded(Frame frame);

    /**
     * Sends an error frame to the client and leaves the client connected
     * @param throwable the error that occurred
     * @return a {@link Promise} that will be completed when the data is successfully sent.
     *         Will be failed if there is a problem sending the data or the underlying TCP connection is already closed.
     */
    Promise<Void> sendError(Throwable throwable);

    /**
     * Sends an error frame to the client and then disconnects the client per the Stomp Spec
     * @param throwable the error that occurred
     * @return a {@link Promise} that will be completed when the data is successfully sent.
     *         Will be failed if there is a problem sending the data or the underlying TCP connection is already closed.
     */
    Promise<Void> sendErrorAndDisconnect(Throwable throwable);

    /**
     * Pause the client from sending data. it sets the buffer in {@code fetch} mode and clears the actual demand.
     * <p>
     * While it's paused, no data will be sent to the data {@link StompServerHandler}.
     */
    void pause();

    /**
     * Resume reading, and sets the buffer in {@code flowing} mode.
     * <p/>
     * If this has been paused, data receiving recommence on it.
     */
    void resume();

    /**
     * Fetch the specified {@code amount} of elements. If this has been paused, reading will
     * recommence with the specified {@code amount} of items, otherwise the specified {@code amount} will
     * be added to the current client demand.
     */
    void fetch(long amount);

    /**
     * Closes the connection with the client.
     */
    void close();

}
