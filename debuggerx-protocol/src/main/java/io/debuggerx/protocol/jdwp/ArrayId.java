package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ArrayId extends ObjectId {
    public static ArrayId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ArrayId(byteBuffer, idSizes);
    }

    ArrayId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ArrayId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
