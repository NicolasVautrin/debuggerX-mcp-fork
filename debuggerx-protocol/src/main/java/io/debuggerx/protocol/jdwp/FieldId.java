package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 *
 * @author ouwu
 */
public class FieldId extends DataTypeBase {
    public static FieldId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new FieldId(byteBuffer, idSizes);
    }

    FieldId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, IdSizes.SizeType.FIELD_ID_SIZE, idSizes);
    }

    public FieldId(long value, IdSizes idSizes) {
        super(value, IdSizes.SizeType.FIELD_ID_SIZE, idSizes);
    }
}
