package io.debuggerx.protocol.jdwp;

import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ReferenceTypeId extends ObjectId {
    public static ReferenceTypeId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ReferenceTypeId(byteBuffer, idSizes);
    }

    ReferenceTypeId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public ReferenceTypeId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
