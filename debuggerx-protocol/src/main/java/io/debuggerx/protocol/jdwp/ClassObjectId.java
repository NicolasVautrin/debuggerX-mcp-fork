package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ClassObjectId extends ObjectId {
    public static ClassObjectId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ClassObjectId(byteBuffer, idSizes);
    }

    ClassObjectId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ClassObjectId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
