package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class StringId extends ObjectId {
    public static StringId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new StringId(byteBuffer, idSizes);
    }

    StringId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public StringId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
