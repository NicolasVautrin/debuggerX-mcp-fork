package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ClassLoaderId extends ObjectId {
    public static ClassLoaderId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ClassLoaderId(byteBuffer, idSizes);
    }

    ClassLoaderId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ClassLoaderId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
