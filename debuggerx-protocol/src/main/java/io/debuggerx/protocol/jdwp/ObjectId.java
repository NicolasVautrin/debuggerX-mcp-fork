package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class ObjectId extends DataTypeBase {
    public static ObjectId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new ObjectId(byteBuffer, idSizes);
    }

    ObjectId(ByteBuffer byteBuffer, IdSizes idSizes) {
        super(byteBuffer, IdSizes.SizeType.OBJECT_ID_SIZE, idSizes);
    }

    public ObjectId(long value, IdSizes idSizes) {
        super(value, IdSizes.SizeType.OBJECT_ID_SIZE, idSizes);
    }
}
