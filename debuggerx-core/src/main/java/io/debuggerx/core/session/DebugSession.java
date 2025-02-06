package io.debuggerx.core.session;


import io.debuggerx.common.utils.ChannelUtils;
import io.debuggerx.common.utils.SessionUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DebugSessionManager
 * @author ouwu
 */
@Data
@Slf4j
public class DebugSession {
    /**
     * 会话ID
     */
    private final String sessionId;
    /**
     * 提供调试服务的channel
     */
    private final Channel jvmServerChannel;
    /**
     * 调试器channels
     * key:调试器地址
     * value:调试器channel
     */
    private final Map<String, Channel> debuggerChannels;
    /**
     * 是否与调试服务的jvm完成握手
     */
    private volatile boolean handshakeCompleted;
    /**
     * 数据包全局唯一id
     */
    private final AtomicInteger jvmServerPacketId;
    /**
     * 数据包id映射
     * key:全局唯一id
     * value:原数据包id
     */
    private final Map<Integer, Integer> packetIdMap;
    public DebugSession(Channel jvmServerChannel) {
        this.sessionId = SessionUtils.generateSessionId();
        this.jvmServerChannel = jvmServerChannel;
        this.debuggerChannels = new ConcurrentHashMap<>();
        this.handshakeCompleted = false;
        this.jvmServerPacketId = new AtomicInteger(Integer.MAX_VALUE);
        this.packetIdMap = new ConcurrentHashMap<>();
    }
    
    public void addDebugger(Channel debuggerChannel) {
        debuggerChannels.put(ChannelUtils.getDebugChannelId(debuggerChannel), debuggerChannel);
    }
    
    /**
     * 移除调试器连接
     * @param debuggerChannel 调试器通道
     * @return 被移除的通道ID，如果未找到则返回null
     */
    public String removeDebugger(Channel debuggerChannel) {
        String removedChannelId = null;
        for (Map.Entry<String, Channel> entry : debuggerChannels.entrySet()) {
            if (ChannelUtils.getDebugChannelId(entry.getValue()).equals(ChannelUtils.getDebugChannelId(debuggerChannel))) {
                removedChannelId = entry.getKey();
                Channel channel = debuggerChannels.remove(removedChannelId);
                channel.close();
                break;
            }
        }
        return removedChannelId;
    }
    
    public void broadcast(Object msg) {
        // 只向活跃的channel发送
        debuggerChannels.values().stream()
                .filter(Channel::isActive)
                .forEach(debuggerChannel -> {
                    ChannelFuture future = debuggerChannel.writeAndFlush(msg);
                    future.addListener(f -> {
                        if (!f.isSuccess()) {
                            log.error("Failed to forward packet to debugger", f.cause());
                        }
                    });
                });
    }

    public int getNewIdAndSaveOriginLink(int originId) {
        int newId = jvmServerPacketId.decrementAndGet();
        packetIdMap.put(newId, originId);
        return newId;
    }

    public int getOriginIdByNewId(int newId) {
        return packetIdMap.getOrDefault(newId, newId);
    }
} 