package io.debuggerx.core.session;


import io.debuggerx.common.utils.ChannelUtils;
import io.debuggerx.common.utils.SessionUtils;
import io.debuggerx.protocol.packet.BreakpointRequestRelation;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.debuggerx.protocol.packet.PacketSource;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a debug session connecting one JVM to multiple debugger clients.
 * Manages packet ID mapping, request ID tracking, breakpoint registry, and event routing.
 * Thread-safe for concurrent access from multiple debugger clients.
 *
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
     * value:Pair<原数据包id, 原数据包Channel>
     */
    private final Map<Integer, Pair<Integer, PacketSource>> packetIdMap;
    /**
     * key:全局唯一id
     * value:数据包
     */
    private final Map<Integer, JdwpPacket> packetMap;
    /**
     * 请求ID映射
     * key:请求ID
     * value:请求通道
     */
    private final Map<Integer, Set<PacketSource>> eventRequestIdSourceMap;
    /**
     * 客户端断点类型
     * key: 客户端channel
     * value: 断点-requestId
     */
    @Getter
    private final Map<Channel, Set<BreakpointRequestRelation>> breakpointRequestMap;
    /**
     * Global breakpoint registry - tracks ALL breakpoints from ALL clients
     * key: requestId
     * value: breakpoint details
     */
    @Getter
    private final Map<Integer, io.debuggerx.protocol.packet.BreakpointInfo> globalBreakpoints;
    /**
     * Pending resolution queries - maps JDWP query packet ID to breakpoint requestId
     * Used to update breakpoint info when resolution queries return
     */
    @Getter
    private final Map<Integer, Integer> pendingResolutions;
    /**
     * Current breakpoint event (for Inspector injection and debugging)
     * Updated whenever a BREAKPOINT event is received
     */
    @Getter
    private volatile io.debuggerx.protocol.packet.BreakpointEventInfo currentBreakpointEvent;

    public DebugSession(Channel jvmServerChannel) {
        this.sessionId = SessionUtils.generateSessionId();
        this.jvmServerChannel = jvmServerChannel;
        this.debuggerChannels = new ConcurrentHashMap<>();
        this.handshakeCompleted = false;
        this.jvmServerPacketId = new AtomicInteger(Integer.MAX_VALUE);
        this.packetIdMap = new ConcurrentHashMap<>();
        this.eventRequestIdSourceMap = new ConcurrentHashMap<>();
        this.packetMap = new ConcurrentHashMap<>();
        this.breakpointRequestMap = new ConcurrentHashMap<>();
        this.globalBreakpoints = new ConcurrentHashMap<>();
        this.pendingResolutions = new ConcurrentHashMap<>();
    }

    public void addDebugger(Channel debuggerChannel) {
        debuggerChannels.put(ChannelUtils.getDebugChannelId(debuggerChannel), debuggerChannel);
    }
    
    /**
     * Removes a debugger client connection from this session.
     *
     * @param debuggerChannel the debugger channel to remove
     * @return the removed channel ID, or null if not found
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

    public int getNewIdAndSaveOriginLink(JdwpPacket packet, PacketSource originPacketSource) {
        int originId = packet.getHeader().getId();
        int newId = jvmServerPacketId.decrementAndGet();
        packetIdMap.put(newId, Pair.of(originId, originPacketSource));
        packetMap.put(newId, packet);
        return newId;
    }

    public JdwpPacket findPacketByNewId(int newId) {
        return packetMap.get(newId);
    }

    public Pair<Integer, PacketSource> getOriginIdByNewId(int newId) {
        return packetIdMap.getOrDefault(newId, null);
    }

    public void cacheRequestIdSourceChannel(Integer requestId, PacketSource packetSource, JdwpPacket packet) {
        if (!packet.getHeader().isCommand()) {
            // 如果是回复包 找到原始的命令包 找到原始的命令包来源
            Pair<Integer, PacketSource> packetSourcePair = packetIdMap.get(packet.getHeader().getId());
            packetSource = packetSourcePair.getRight();
        }
        eventRequestIdSourceMap.computeIfAbsent(requestId, id -> new CopyOnWriteArraySet<>());
        Set<PacketSource> sourceChannels = eventRequestIdSourceMap.get(requestId);
        if (sourceChannels.contains(packetSource)) {
            return;
        }
        sourceChannels.add(packetSource);
    }

    public Set<PacketSource> findSourceChannelByRequestId(Integer requestId) {
        return eventRequestIdSourceMap.get(requestId);
    }

    /**
     * Caches a request ID for a specific channel without replacing existing sources.
     * Used for broadcasting events to multiple debugger clients.
     *
     * @param requestId the JDWP request ID
     * @param packetSource the packet source to associate with this request ID
     */
    public void cacheRequestIdForChannel(Integer requestId, PacketSource packetSource) {
        eventRequestIdSourceMap.computeIfAbsent(requestId, id -> new CopyOnWriteArraySet<>());
        Set<PacketSource> sourceChannels = eventRequestIdSourceMap.get(requestId);
        if (sourceChannels.contains(packetSource)) {
            return;
        }
        sourceChannels.add(packetSource);
    }

    /**
     * Updates the current breakpoint event context (called when a BREAKPOINT event is received).
     * Stores thread ID and breakpoint metadata for debugging and inspection.
     *
     * @param eventInfo the breakpoint event information (thread ID, breakpoint details)
     */
    public void setCurrentBreakpointEvent(io.debuggerx.protocol.packet.BreakpointEventInfo eventInfo) {
        this.currentBreakpointEvent = eventInfo;
        io.debuggerx.protocol.packet.BreakpointInfo bp = eventInfo.getBreakpoint();
        log.info("[DebugSession] BREAKPOINT hit: threadId={}, requestId={}, {}:{} (line {})",
            eventInfo.getThreadId(),
            bp.getRequestId(),
            bp.getClassName(),
            bp.getMethodName(),
            bp.getLineNumber());
    }

}