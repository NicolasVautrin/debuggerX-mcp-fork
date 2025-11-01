package io.debuggerx.core.strategy.impl;

import io.debuggerx.common.enums.ConnectionType;
import io.debuggerx.common.utils.CollectionUtils;
import io.debuggerx.core.service.DebuggerService;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.core.strategy.ConnectionHandlerStrategy;
import io.debuggerx.protocol.packet.BreakpointRequestRelation;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.debuggerx.protocol.packet.PacketSource;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Handles packets from debugger clients (IntelliJ, Eclipse, MCP, etc.).
 * Forwards commands to JVM and clears client breakpoints on disconnect.
 *
 * @author ouwu
 */
@Slf4j
public class DebuggerProxyStrategy implements ConnectionHandlerStrategy {
    @Override
    public void handle(ChannelHandlerContext ctx, JdwpPacket packet, DebuggerService service) {
        this.handleDebuggerProxyPacket(ctx.channel(), packet, service);
    }

    public void handleDebuggerProxyPacket(Channel channel, JdwpPacket packet, DebuggerService service) {
        if (packet.getHeader().isDisposeCommand()) {
            log.info("[Dispose command] Dispose command received, closing debugger channel: {}", channel);
            sendClearAllBreakpointsCommand(channel);
            channel.close();
            return;
        }
        // 查找对应的调试目标会话并转发
        DebugSession session = SessionManager.getInstance().findJvmServerSession();
        // 检查channel状态
        if (session.getJvmServerChannel().isActive()) {
            service.handlePacket(new PacketSource(ConnectionType.DEBUGGER_PROXY, channel), packet, session);

            ChannelFuture future = session.getJvmServerChannel().writeAndFlush(packet);
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    log.error("[JvmServer]Failed to forward packet to jvm server", f.cause());
                }
            });
        }
    }

    private void sendClearAllBreakpointsCommand(Channel channel) {
        DebugSession session = SessionManager.getInstance().findJvmServerSession();

        Set<BreakpointRequestRelation> breakpointRequestRelations = session.getBreakpointRequestMap().get(channel);
        if (CollectionUtils.isEmpty(breakpointRequestRelations)) {
            return;
        }

        for (BreakpointRequestRelation breakpointRequestRelation : breakpointRequestRelations) {
            JdwpPacket packet = JdwpPacket.createClearBreakpointsPacket(breakpointRequestRelation.toByteArray());
            int newId = session.getNewIdAndSaveOriginLink(packet, new PacketSource(ConnectionType.DEBUGGER_PROXY, channel));
            packet.getHeader().setId(newId);
            session.getJvmServerChannel().writeAndFlush(packet);
        }
    }

}
