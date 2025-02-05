package io.debuggerx.common.utils;

import java.util.UUID;

/**
 * SessionUtils
 * @author ouwu
 */
public class SessionUtils {
    private SessionUtils() {

    }
    /**
     * 生成会话ID
     * @return 唯一的会话ID
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

} 