package io.debuggerx.protocol.jdwp;

import io.debuggerx.protocol.enums.CommandIdentifier;

import java.nio.ByteBuffer;

/**
 * CommandIdentifier.ID_SIZES_REPLY
 *
 * {@link CommandIdentifier}
 * @author ouwu
 */
public class IdSizes {
    private final int fieldIdSize;
    private final int methodIdSize;
    private final int objectIdSize;
    private final int referenceTypeIdSize;
    private final int frameIdSize;

    public static IdSizes read(ByteBuffer byteBuffer) {
        return new IdSizes(
                byteBuffer.getInt(),
                byteBuffer.getInt(),
                byteBuffer.getInt(),
                byteBuffer.getInt(),
                byteBuffer.getInt()
        );
    }

    public IdSizes(int fieldIdSize, int methodIdSize, int objectIdSize, int referenceTypeIdSize, int frameIdSize) {
        this.fieldIdSize = fieldIdSize;
        this.methodIdSize = methodIdSize;
        this.objectIdSize = objectIdSize;
        this.referenceTypeIdSize = referenceTypeIdSize;
        this.frameIdSize = frameIdSize;
    }

    public int getSizeOfType(SizeType sizeType) {
        switch (sizeType) {
            case FIELD_ID_SIZE:
                return getFieldIdSize();
            case METHOD_ID_SIZE:
                return getMethodIdSize();
            case OBJECT_ID_SIZE:
                return getObjectIdSize();
            case REFERENCE_TYPE_ID_SIZE:
                return getReferenceTypeIdSize();
            case FRAME_ID_SIZE:
            default:
                return getFrameIdSize();
        }
    }

    public int getFieldIdSize() {
        return fieldIdSize;
    }

    public int getMethodIdSize() {
        return methodIdSize;
    }

    public int getObjectIdSize() {
        return objectIdSize;
    }

    public int getReferenceTypeIdSize() {
        return referenceTypeIdSize;
    }

    public int getFrameIdSize() {
        return frameIdSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IdSizes idSizes = (IdSizes) o;

        if (fieldIdSize != idSizes.fieldIdSize) {
            return false;
        }
        if (methodIdSize != idSizes.methodIdSize) {
            return false;
        }
        if (objectIdSize != idSizes.objectIdSize) {
            return false;
        }
        if (referenceTypeIdSize != idSizes.referenceTypeIdSize) {
            return false;
        }
        return frameIdSize == idSizes.frameIdSize;
    }

    @Override
    public int hashCode() {
        int result = fieldIdSize;
        result = 31 * result + methodIdSize;
        result = 31 * result + objectIdSize;
        result = 31 * result + referenceTypeIdSize;
        result = 31 * result + frameIdSize;
        return result;
    }

    @Override
    public String toString() {
        return "IdSizes{" +
                "fieldIdSize=" + fieldIdSize +
                ", methodIdSize=" + methodIdSize +
                ", objectIdSize=" + objectIdSize +
                ", referenceTypeIdSize=" + referenceTypeIdSize +
                ", frameIdSize=" + frameIdSize +
                '}';
    }

    public enum SizeType {
        /**
         * 对应jfieldID类型
         * 典型值：HotSpot 64位开启压缩指针时为4字节，禁用压缩指针为8字节
         * 影响场景：字段断点设置、反射式字段访问
         */
        FIELD_ID_SIZE,
        /**
         * 对应jmethodID类型
         * 内存布局：通常包含方法元数据地址+修饰符位掩码
         * 特殊案例：Lambda表达式生成的方法ID可能采用扩展编码
         */
        METHOD_ID_SIZE,
        /**
         * 对应jobject引用类型
         * 与JVM指针长度直接相关，但可能因垃圾回收器类型（ZGC/Shenandoah）采用特殊编码
         */
        OBJECT_ID_SIZE,
        /**
         * 覆盖类/接口/数组类型的标识
         * 调试技巧：通过该值判断是否启用类重定义（Redefine Classes）功能
         */
        REFERENCE_TYPE_ID_SIZE,
        /**
         * 复合结构：通常包含线程ID（高位） + 栈帧序号（低位）
         * 动态变化：当启用栈帧合并优化时可能采用压缩编码
         */
        FRAME_ID_SIZE
    }
}
