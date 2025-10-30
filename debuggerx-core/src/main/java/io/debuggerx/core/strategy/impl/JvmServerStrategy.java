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

        // Check if this is an EventRequest.Set reply (CommandSet=15, Command=1)
        // If so, broadcast to ALL clients so they all register the breakpoint
        if (isEventRequestSetReply(packet)) {
            log.info("[EventRequestBroadcast] Broadcasting EventRequest.Set reply to all clients");
            List<Channel> allChannels = session.getDebuggerChannels().values().stream()
                .filter(Channel::isActive)
                .collect(Collectors.toList());
            this.broadcast(packet, allChannels);
        } else {
            // Normal behavior: send only to the requesting client
            this.broadcast(packet, collect.stream().map(PacketSource::getChannel).collect(Collectors.toList()));
        }
    }

    private boolean isEventRequestSetReply(JdwpPacket packet) {
        // EventRequest command set = 15, Set command = 1
        // Reply packets have the same command set/command as the request
        return !packet.getHeader().isCommand() &&
               packet.getHeader().getCommandSet() == 15 &&
               packet.getHeader().getCommand() == 1;
    }

    public void broadcast(Object msg, List<Channel> channels) {
        // 只向活跃的channel发送
        channels.stream()
                .filter(Channel::isActive)
                .forEach(debuggerChannel -> {
                    debuggerChannel.writeAndFlush(msg);
                });
    }

}
