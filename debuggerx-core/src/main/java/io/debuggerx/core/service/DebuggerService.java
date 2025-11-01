package io.debuggerx.core.service;

import io.debuggerx.common.enums.ConnectionType;
import io.debuggerx.common.utils.ChannelUtils;
import io.debuggerx.common.utils.CollectionUtils;
import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.processor.registry.CommandProcessorRegistry;
import io.debuggerx.core.processor.registry.EventProcessorRegistry;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.enums.CommandIdentifier;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.debuggerx.protocol.packet.PacketSource;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core debugging service that manages communication between the JVM and multiple debugger clients.
 * Handles packet routing, request ID mapping, and event broadcasting to all connected debuggers.
 * Implements singleton pattern for global access across the proxy.
 *
 * @author ouwu
 */
@Slf4j
public class DebuggerService {

    private static volatile DebuggerService instance;
    private final SessionManager sessionManager = SessionManager.getInstance();

    private final EventProcessorRegistry eventProcessors = new EventProcessorRegistry();
    private final CommandProcessorRegistry commandProcessors = new CommandProcessorRegistry(eventProcessors);

    private DebuggerService() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
    }

    public static DebuggerService getInstance() {
        if (instance == null) {
            synchronized (DebuggerService.class) {
                if (instance == null) {
                    instance = new DebuggerService();
                }
            }
        }
        return instance;
    }

    public List<PacketSource> handlePacket(PacketSource packetSource, JdwpPacket packet, DebugSession session) {
        Pair<Integer, PacketSource> origin = session.getOriginIdByNewId(packet.getHeader().getId());

        return Objects.isNull(origin)
                ? handleNewPacket(packetSource, packet, session)
                : handleResponsePacket(packet, origin);
    }

    private List<PacketSource> handleNewPacket(PacketSource source, JdwpPacket packet, DebugSession session) {
        // 来源数据包不存在 判断当前数据包是否是特殊事件
        int newId = session.getNewIdAndSaveOriginLink(packet, source);
        packet.getHeader().setId(newId);
        cacheRequestId(source, packet);

        return extractEventSources(packet, session);
    }

    private List<PacketSource> handleResponsePacket(JdwpPacket packet, Pair<Integer, PacketSource> origin) {
        // 来源数据包存在 即当前数据包为回复包
        packet.getHeader().setId(origin.getLeft());
        return Collections.singletonList(origin.getRight());
    }

    private List<PacketSource> extractEventSources(JdwpPacket packet, DebugSession session) {
        if (CollectionUtils.isEmpty(packet.getRequestIds())) {
            return Collections.emptyList();
        }

        return packet.getRequestIds().stream()
                .map(session::findSourceChannelByRequestId)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .collect(Collectors.toList());
    }


    public void handleHandshake(Channel channel, ConnectionType connectionType) {
        switch (connectionType) {
            case JVM_SERVER:
                DebugSession session = sessionManager.createJvmServerSession(channel);
                session.setHandshakeCompleted(true);
                break;
            case DEBUGGER_PROXY:
                DebugSession debugSession = sessionManager.findJvmServerSession();
                debugSession.addDebugger(channel);
                log.info("[DebuggerHandShake] Debugger register to jvm server: {}", ChannelUtils.getDebugChannelId(channel));
                break;
            default:
                break;
        }
    }

    public void handleDisconnect(Channel channel, ConnectionType connectionType) {
        DebugSession jvmServerSession = sessionManager.findJvmServerSession();
        switch (connectionType) {
            case JVM_SERVER:
                // 被调试程序断开，关闭整个会话
                log.info("[Disconnect] Jvm server disconnected, closing session: {}", jvmServerSession.getSessionId());
                sessionManager.removeSession(channel);
                break;
            case DEBUGGER_PROXY:
                String removedChannelId = jvmServerSession.removeDebugger(channel);
                log.info("[DebuggerProxyDisconnect] Debugger disconnected from session: {}, channel: {}",
                        jvmServerSession.getSessionId(), removedChannelId);
                break;
            default:
                break;
        }
    }

    /**
     * Caches request IDs from JDWP packets for event routing.
     * Only event-related commands contain request IDs (e.g., EventRequest.Set, EventRequest.Clear).
     * Request IDs are broadcast to all connected debugger clients for proper event distribution.
     *
     * @param packetSource the packet source (JVM or debugger client)
     * @param packet the JDWP packet to process
     */
    public void cacheRequestId(PacketSource packetSource, JdwpPacket packet) {
        // 处理回复包的命令集映射
        if (!packet.getHeader().isCommand()) {
            mapResponseCommand(packet);
        }

        // 获取命令处理器
        CommandIdentifier commandId = CommandIdentifier.of(packet.getHeader());
        CommandProcessor processor = commandProcessors.getProcessor(commandId);
        if (processor == null) {
            // 无需处理
            return;
        }

        // 执行命令处理
        ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
        List<Integer> requestIds = processor.process(buffer, packet);
        packet.setRequestIds(requestIds);

        // 缓存结果
        cacheRequestIds(packetSource, packet, requestIds);
    }

    private void mapResponseCommand(JdwpPacket packet) {
        DebugSession session = SessionManager.getInstance().findJvmServerSession();
        JdwpPacket originPacket = session.findPacketByNewId(packet.getHeader().getId());

        // Guard against null for unexpected cases
        if (originPacket == null) {
            log.warn("[mapResponseCommand] No origin packet found for ID {}, cannot map command",
                packet.getHeader().getId());
            return;
        }

        packet.getHeader().setCommandSet(originPacket.getHeader().getCommandSet());
        packet.getHeader().setCommand(originPacket.getHeader().getCommand());
    }

    private void cacheRequestIds(PacketSource source, JdwpPacket packet, List<Integer> requestIds) {
        if (CollectionUtils.isEmpty(requestIds)) {
            return;
        }

        DebugSession session = sessionManager.findJvmServerSession();

        // ALL JDWP events are global to the JVM and should be broadcast to ALL clients
        // Register each requestId for ALL connected debugger clients
        requestIds.forEach(id -> {
            session.getDebuggerChannels().values().forEach(channel -> {
                PacketSource clientSource = new PacketSource(source.getSourceType(), channel);
                session.cacheRequestIdSourceChannel(id, clientSource, packet);
            });
        });
    }
}