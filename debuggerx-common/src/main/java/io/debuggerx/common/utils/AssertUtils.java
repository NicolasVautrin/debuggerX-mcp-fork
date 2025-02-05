package io.debuggerx.common.utils;

import io.debuggerx.common.exception.DebuggerException;

/**
 * AssertUtils
 * @author ouwu
 */
public class AssertUtils {
    private AssertUtils() {
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new DebuggerException(message);
        }
    }
}