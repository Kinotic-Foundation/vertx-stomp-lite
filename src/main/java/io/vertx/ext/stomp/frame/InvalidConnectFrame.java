package io.vertx.ext.stomp.frame;

import io.vertx.core.buffer.Buffer;

/**
 * Created by 🤓 on 6/4/21.
 */
public class InvalidConnectFrame extends RuntimeException{

    private final Buffer data;

    public InvalidConnectFrame(String message, Buffer data) {
        super(message);
        this.data = data;
    }

    /**
     * The original connect frame before parsing sent by the client
     * @return the raw data that was considered an invalid connect frame
     */
    public Buffer getData() {
        return data;
    }
}
