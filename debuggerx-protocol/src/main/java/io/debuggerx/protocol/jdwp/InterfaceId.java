package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class InterfaceId extends ReferenceTypeId {
    public static InterfaceId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new InterfaceId(byteBuffer, idSizes);
    }

    InterfaceId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, idSizes);
    }

    public InterfaceId(long value, IdSizes idSizes) {
        super(value, idSizes);
    }
}
