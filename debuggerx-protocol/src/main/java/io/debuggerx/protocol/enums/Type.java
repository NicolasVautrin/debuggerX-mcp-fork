package io.debuggerx.protocol.enums;

import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.ObjectId;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Function;

/**
 * 方法返回值类型
 * @author ouwu
 */

public enum Type {
    /**
     * Array type
     */
    ARRAY(91, pair -> ObjectId.read(pair.getLeft(), pair.getRight())),
    /**
     * BYTE
     */
    BYTE(66, pair -> pair.getLeft().get()),
    /**
     * CHAR
     */
    CHAR(67, pair -> pair.getLeft().getShort()),
    /**
     * OBJECT
     */
    OBJECT(76, pair -> ObjectId.read(pair.getLeft(), pair.getRight())),
    /**
     * FLOAT
     */
    FLOAT(70, pair -> pair.getLeft().getFloat()),
    /**
     * DOUBLE
     */
    DOUBLE(68, pair -> pair.getLeft().getDouble()),
    /**
     * INT
     */
    INT(73, pair -> pair.getLeft().getInt()),
    /**
     * LONG
     */
    LONG(74, pair -> pair.getLeft().getLong()),
    /**
     * SHORT
     */
    SHORT(83, pair -> pair.getLeft().getShort()),
    /**
     * VOID
     */
    VOID(86, pair -> null),
    /**
     * BOOLEAN
     */
    BOOLEAN(90, pair -> pair.getLeft().get() != 0),
    /**
     * STRING
     */
    STRING(115, pair -> ObjectId.read(pair.getLeft(), pair.getRight())),
    /**
     * THREAD
     */
    THREAD(116, pair -> ObjectId.read(pair.getLeft(), pair.getRight())),
    /**
     * THREAD_GROUP
     */
    THREAD_GROUP(103, pair -> ObjectId.read(pair.getLeft(), pair.getRight())),
    /**
     * CLASS_LOADER
     */
    CLASS_LOADER(108, pair -> ObjectId.read(pair.getLeft(), pair.getRight())),
    /**
     * CLASS_OBJECT
     */
    CLASS_OBJECT(99, pair -> ObjectId.read(pair.getLeft(), pair.getRight()));

    private final byte id;
    private final Function<Pair<ByteBuffer, IdSizes>, Object> readFunction;

    Type(int value, Function<Pair<ByteBuffer, IdSizes>, Object> readFunction) {
        this.id = (byte) value;
        this.readFunction = readFunction;
    }

    public static Type findByValue(byte value) {
        return Arrays.stream(Type.values())
                .filter(type -> type.id == value)
                .findFirst().get();
    }

    public Object read(Pair<ByteBuffer, IdSizes> input) {
        return readFunction.apply(input);
    }

    public byte getId() {
        return id;
    }
}
