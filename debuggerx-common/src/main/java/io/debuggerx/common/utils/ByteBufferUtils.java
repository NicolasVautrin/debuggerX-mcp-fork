package io.debuggerx.common.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author ouwu
 */
public class ByteBufferUtils {
    public static Long getLong(ByteBuffer buffer, int size) {
        if (size > 8 || size < 0) {
            throw new UnsupportedOperationException("malformed input");
        }
        int offset = 8 - size;
        byte[] bytes = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        buffer.get(bytes, offset, size);
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static String getString(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes, 0, length);

        return new String(bytes, StandardCharsets.UTF_8);
    }

}
