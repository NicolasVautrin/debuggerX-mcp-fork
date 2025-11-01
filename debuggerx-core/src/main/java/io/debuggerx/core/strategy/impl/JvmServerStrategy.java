package io.debuggerx.core.strategy.impl;

import io.debuggerx.common.enums.ConnectionType;
import io.debuggerx.common.utils.CollectionUtils;
import io.debuggerx.core.service.DebuggerService;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.core.strategy.ConnectionHandlerStrategy;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.debuggerx.protocol.packet.PacketSource;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles packets from the JVM server.
 * Broadcasts events and replies to all active debugger clients.
 *
 * @author ouwu
 */
@Slf4j
public class JvmServerStrategy implements ConnectionHandlerStrategy {
    @Override
    public void handle(ChannelHandlerContext ctx, JdwpPacket packet, DebuggerService service) {
        this.handleJvmServerPacket(packet, service);
    }

    public void handleJvmServerPacket(JdwpPacket packet, DebuggerService service) {
        DebugSession session = SessionManager.getInstance().findJvmServerSession();
        if (session == null) {
            log.error("No session found for jvm server session. Is command packet?:{}", packet.getHeader().isCommand());
            return;
        }
        List<PacketSource> packetSources = service.handlePacket(new PacketSource(ConnectionType.JVM_SERVER, session.getJvmServerChannel()), packet, session);
        if (CollectionUtils.isEmpty(packetSources)) {
            log.error("Channel is null.");
            return;
        }
        List<PacketSource> collect = packetSources.stream().filter(s -> s.getSourceType().equals(ConnectionType.DEBUGGER_PROXY)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            log.error("The reply channel is empty.");
            return;
        }
        this.broadcast(packet, collect.stream().map(PacketSource::getChannel).collect(Collectors.toList()));
    }

    /**
     * Broadcasts a message to multiple debugger clients.
     * Only sends to active channels.
     *
     * @param msg the message to broadcast
     * @param channels the list of debugger channels
     */
    public void broadcast(Object msg, List<Channel> channels) {
        channels.stream()
                .filter(Channel::isActive)
                .forEach(debuggerChannel -> {
                    debuggerChannel.writeAndFlush(msg);
                });
    }

}
