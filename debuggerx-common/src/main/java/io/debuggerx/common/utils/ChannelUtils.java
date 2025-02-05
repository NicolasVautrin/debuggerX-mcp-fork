package io.debuggerx.common.utils;

import io.netty.channel.Channel;

/**
 * ChannelUtils
 * @author ouwu
 */
public class ChannelUtils {
    private ChannelUtils() {
    }

    /**
     * 生成会话ID
     * @return 唯一的会话ID
     */
    public static String getDebugChannelId(Channel channel) {
        return channel.remoteAddress().toString();
    }

} 