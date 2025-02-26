package io.debuggerx.protocol.jdwp;

import io.debuggerx.protocol.enums.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class Value {
    private final Type type;
    private final Object returnValue;

    public static Value read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new Value(byteBuffer, idSizes);
    }

    private Value(ByteBuffer byteBuffer, IdSizes idSizes) {
        this(byteBuffer, idSizes, byteBuffer.get());
    }

    protected Value(ByteBuffer byteBuffer, IdSizes idSizes, byte typeTag) {
        this.type = Type.findByValue(typeTag);
        returnValue = type.read(Pair.of(byteBuffer, idSizes));
    }

    public Value(Type type, Object value) {
        this.type = type;
        this.returnValue = value;
    }

    public Type getType() {
        return type;
    }

    public Object getReturnValue() {
        return returnValue;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Value value1 = (Value) o;

        if (type != value1.type) {
            return false;
        }
        return returnValue != null ? returnValue.equals(value1.returnValue) : value1.returnValue == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (returnValue != null ? returnValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Value{" +
                "type=" + type +
                ", returnValue=" + returnValue +
                '}';
    }
}
