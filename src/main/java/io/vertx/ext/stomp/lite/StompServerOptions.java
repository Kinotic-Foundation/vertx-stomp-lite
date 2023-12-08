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

import io.vertx.ext.stomp.lite.frame.Frame;
import io.vertx.core.json.JsonObject;

/**
 * STOMP Server options. You can also configure the Net Server used by the STOMP server from these options.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StompServerOptions {

  public static final int DEFAULT_MAX_CONNECT_FRAME_LENGTH = 1024 * 10;
  public static final int DEFAULT_MAX_HEADER_LENGTH = 1024 * 10;
  public static final int DEFAULT_MAX_HEADERS = 1000;
  public static final int DEFAULT_MAX_BODY_LENGTH = 1024 * 1024 * 10;

  public static final String DEFAULT_WEBSOCKET_PATH = "/stomp";
  public static int DEFAULT_STOMP_PORT = 61613;
  public static String DEFAULT_STOMP_HOST = "0.0.0.0";

  public static JsonObject DEFAULT_STOMP_HEARTBEAT = new JsonObject().put("x", 30000).put("y", 30000);
  public static boolean DEFAULT_TRAILING_LINE = false;
  public static boolean DEFAULT_DEBUG_ENABLED = false;

  private int maxConnectFrameLength = DEFAULT_MAX_CONNECT_FRAME_LENGTH;
  private int maxHeaderLength = DEFAULT_MAX_HEADER_LENGTH;
  private int maxHeaders = DEFAULT_MAX_HEADERS;
  private int maxBodyLength = DEFAULT_MAX_BODY_LENGTH;

  private JsonObject heartbeat = DEFAULT_STOMP_HEARTBEAT;

  private String websocketPath = DEFAULT_WEBSOCKET_PATH;
  private boolean trailingLine = DEFAULT_TRAILING_LINE;
  private int port;
  private String host;
  private boolean debugEnabled = DEFAULT_DEBUG_ENABLED;

  /**
   * Default constructor.
   */
  public StompServerOptions() {
    super();
    setPort(DEFAULT_STOMP_PORT);
    setHost(DEFAULT_STOMP_HOST);
  }

  /**
   * Gets the maximum length of the CONNECT frame that can be provided by the client
   * @return the max length of the connect frame in bytes
   */
  public int getMaxConnectFrameLength() {
    return maxConnectFrameLength;
  }

  /**
   * Sets the maximum length of the CONNECT frame that can be provided by the client
   * @param maxConnectFrameLength the max length of the connect frame in bytes
   * @return the current {@link StompServerOptions}
   */
  public StompServerOptions setMaxConnectFrameLength(int maxConnectFrameLength) {
    this.maxConnectFrameLength = maxConnectFrameLength;
    return this;
  }

  /**
   * Gets the max length of the frame body accepted by the server. If a frame exceeds this size, the frame is
   * rejected and an error is sent to the client.
   *
   * @return the max body length in bytes
   */
  public int getMaxBodyLength() {
    return maxBodyLength;
  }

  /**
   * Sets the max body length accepted by the server. 100 Mb by default.
   *
   * @param maxBodyLength the length in bytes.
   * @return the current {@link StompServerOptions}
   */
  public StompServerOptions setMaxBodyLength(int maxBodyLength) {
    this.maxBodyLength = maxBodyLength;
    return this;
  }

  /**
   * Gets the max length of header's value. If a frame has an header with a value exceeding this length, the frame is
   * rejected and an error is sent to the client. 10240 by default.
   *
   * @return the max header length
   */
  public int getMaxHeaderLength() {
    return maxHeaderLength;
  }

  /**
   * Sets the max header length.
   *
   * @param maxHeaderLength the max length of headers
   * @return the current {@link StompServerOptions}
   */
  public StompServerOptions setMaxHeaderLength(int maxHeaderLength) {
    this.maxHeaderLength = maxHeaderLength;
    return this;
  }

  /**
   * Gets the maximum number of headers supported by the server. If a frame exceeds the number of headers, the frame
   * is rejected and an error is sent to the client.
   *
   * @return the max number of headers
   */
  public int getMaxHeaders() {
    return maxHeaders;
  }

  /**
   * Sets the maximum number of headers. 1000 by default.
   *
   * @param maxHeaders the number of headers
   * @return the current {@link StompServerOptions}
   */
  public StompServerOptions setMaxHeaders(int maxHeaders) {
    this.maxHeaders = maxHeaders;
    return this;
  }

  /**
   * Sets the port on which the server is going to listen for connections.
   *
   * @param port the port number
   * @return the current {@link StompServerOptions}.
   */
  public StompServerOptions setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * Set the host
   * @param host  the host
   * @return a reference to this, so the API can be used fluently
   */
  public StompServerOptions setHost(String host) {
    this.host = host;
    return this;
  }

  /**
   *
   * @return the host
   */
  public String getHost(){
    return host;
  }

  /**
   * Gets the heartbeat configuration. Defaults to {@code x: 1000, y: 1000}.
   *
   * @return the heartbeat configuration.
   * @see Frame.Heartbeat
   */
  public JsonObject getHeartbeat() {
    return heartbeat;
  }

  /**
   * Sets the heartbeat configuration.
   *
   * @param heartbeat the heartbeat configuration
   * @return the current {@link StompServerOptions}
   * @see Frame.Heartbeat
   */
  public StompServerOptions setHeartbeat(JsonObject heartbeat) {
    this.heartbeat = heartbeat;
    return this;
  }

  /**
   * Gets the path for the web socket. Only web sockets frame receiving on this path would be handled. By default
   * it's {@link #DEFAULT_WEBSOCKET_PATH}. The returned String is not a prefix but an exact match.
   *
   * @return the path
   */
  public String getWebsocketPath() {
    return websocketPath;
  }

  /**
   * Sets the websocket path. Only frames received on this path would be considered as STOMP frame.
   *
   * @param websocketPath the path, must not be {@code null}.
   * @return the current {@link StompServerOptions}
   */
  public StompServerOptions setWebsocketPath(String websocketPath) {
    this.websocketPath = websocketPath;
    return this;
  }

  /**
   * Gets whether or not an empty line should be appended to the written STOMP frame. This option is disabled by
   * default. This option is not compliant with the STOMP specification, and so is not documented on purpose.
   *
   * @return whether or not an empty line should be appended to the written STOMP frame.
   */
  public boolean isTrailingLine() {
    return trailingLine;
  }

  /**
   * Sets whether or not an empty line should be appended to the written STOMP frame. This option is disabled by
   * default. This option is not compliant with the STOMP specification, and so is not documented on purpose.
   *
   * @param trailingLine {@code true} to add an empty line, {@code false} otherwise
   * @return the current {@link StompServerOptions}
   */
  public StompServerOptions setTrailingLine(boolean trailingLine) {
    this.trailingLine = trailingLine;
    return this;
  }

  /**
   * Gets whether or not debug is enabled.
   * Debug tells the stomp server to provide additional information to the client.
   * Such as stack traces upon an error. This is typically disabled in production.
   * @return true if debug is enabled false if not
   */
  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  /**
   * Sets if debug should be enabled or not.
   * @param debugEnabled true to enable debug false to disable it.
   * @return this
   */
  public StompServerOptions setDebugEnabled(boolean debugEnabled) {
    this.debugEnabled = debugEnabled;
    return this;
  }

}
