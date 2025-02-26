package io.debuggerx.protocol.jdwp;

import io.debuggerx.common.utils.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

abstract class DataTypeBase {
    private final static Logger log = LoggerFactory.getLogger(DataTypeBase.class);
    private final long value;
    private final IdSizes.SizeType sizeType;
    private final IdSizes idSizes;


    DataTypeBase(ByteBuffer buffer, IdSizes.SizeType sizeType, IdSizes idSizes) {
        this.sizeType = sizeType;
        this.idSizes = idSizes;
        this.value = this.readLongOfSize(buffer);
    }

    DataTypeBase(long value, IdSizes.SizeType sizeType, IdSizes idSizes) {
        this.value = value;
        this.sizeType = sizeType;
        this.idSizes = idSizes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataTypeBase that = (DataTypeBase) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    public long asLong() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "value=" + value +
                '}';
    }

    public long readLongOfSize(ByteBuffer byteBuffer) {
        int size;
        if (idSizes == null) {
            log.warn("Parsing value without knowing its size in bytes. Assuming size is 8 bytes.");
            size = 8;
        } else {
            size = idSizes.getSizeOfType(sizeType);
        }
        return ByteBufferUtils.getLong(byteBuffer, size);
    }
}
