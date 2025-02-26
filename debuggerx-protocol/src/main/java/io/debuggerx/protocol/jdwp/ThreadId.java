package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ThreadId extends ObjectId {
    public static ThreadId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ThreadId(byteBuffer, idSizes);
    }

    ThreadId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ThreadId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
