package io.debuggerx.protocol.jdwp;

import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ArrayTypeId extends ReferenceTypeId {
    public static ArrayTypeId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ArrayTypeId(byteBuffer, idSizes);
    }

    ArrayTypeId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ArrayTypeId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
