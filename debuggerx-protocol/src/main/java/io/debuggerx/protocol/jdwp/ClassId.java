package io.debuggerx.protocol.jdwp;

import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ClassId extends ReferenceTypeId {
    public static ClassId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ClassId(byteBuffer, idSizes);
    }

    ClassId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ClassId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
