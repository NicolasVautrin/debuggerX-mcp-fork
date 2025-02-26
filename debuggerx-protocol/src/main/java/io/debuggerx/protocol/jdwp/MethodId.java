package io.debuggerx.protocol.jdwp;

import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class MethodId extends DataTypeBase {
    public static MethodId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new MethodId(byteBuffer, idSizes);
    }

    MethodId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, IdSizes.SizeType.METHOD_ID_SIZE, idSizes);
    }

    public MethodId(long value, IdSizes idSizes) {
        super(value, IdSizes.SizeType.METHOD_ID_SIZE, idSizes);
    }
}
