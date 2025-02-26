package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ThreadGroupId extends ObjectId {
    public static ThreadGroupId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ThreadGroupId(byteBuffer, idSizes);
    }

    ThreadGroupId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ThreadGroupId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
