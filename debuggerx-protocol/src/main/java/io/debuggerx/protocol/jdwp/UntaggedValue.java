package io.debuggerx.protocol.jdwp;


import io.debuggerx.protocol.enums.Type;

import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class UntaggedValue extends Value {
    public static UntaggedValue read(ByteBuffer byteBuffer, IdSizes idSizes, byte typeTag) {
        return new UntaggedValue(byteBuffer, idSizes, typeTag);
    }

    UntaggedValue(ByteBuffer byteBuffer, IdSizes idSizes, byte typeTag) {
        super(byteBuffer, idSizes, typeTag);
    }

    public UntaggedValue(Type type, Object value) {
        super(type, value);
    }
}
