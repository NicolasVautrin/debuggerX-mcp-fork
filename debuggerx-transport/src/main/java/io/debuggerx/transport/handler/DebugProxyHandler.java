package io.debuggerx.transport.handler;

import io.debuggerx.common.constants.JdwpConstants;
import io.debuggerx.common.enums.ConnectionType;
import io.debuggerx.core.service.DebuggerService;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import static io.debuggerx.common.enums.ConnectionType.JVM_SERVER;

/**
 * 数据包处理
 *
 * @author ouwu
 */
@Slf4j
@ChannelHandler.Sharable
public class DebugProxyHandler extends ChannelInboundHandlerAdapter {
    private final ConnectionType connectionType;
    private final DebuggerService debuggerService;
    
    public DebugProxyHandler(ConnectionType connectionType) {
        this.connectionType = connectionType;
        this.debuggerService = DebuggerService.getInstance();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        log.info("[DebugProxy] {} channel registered: {}", connectionType, ctx.channel());
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[DebugProxy] {} channel active: {}", connectionType, ctx.channel());
        // 对于JVM客户端主动发送握手信息
        if (connectionType == JVM_SERVER) {
            handleHandShakeMsg(ctx);
        }
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("[DebugProxyHandleMsg] {} channel received message type: {}", connectionType, msg.getClass().getSimpleName());
        
        if (msg instanceof byte[] && new String((byte[]) msg).equals(JdwpConstants.HANDSHAKE_STRING)) {
            handleHandshake(ctx);
            return;
        }
        
        if (msg instanceof JdwpPacket) {
            handlePacket(ctx, (JdwpPacket) msg);
        }
    }

    private void handleHandshake(ChannelHandlerContext ctx) {
        log.debug("[DebugProxyHandleMsg] Handling handshake for {} connection: {}", connectionType, ctx.channel());
        debuggerService.handleHandshake(ctx.channel(), connectionType);
        
        // debug客户端主动握手回复
        if (connectionType == ConnectionType.DEBUGGER_PROXY) {
            handleHandShakeMsg(ctx);
        }
    }

    private void handleHandShakeMsg(ChannelHandlerContext ctx) {
        log.debug("[DebugProxyHandleMsg] {} Sending handshake", ctx.channel());
        ctx.writeAndFlush(Unpooled.wrappedBuffer(JdwpConstants.HANDSHAKE_PACKET))
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.debug("[DebugProxyHandleMsg] Handshake sent successfully: {}", ctx.channel());
                    } else {
                        log.error("[DebugProxyHandleMsg] Failed to send handshake: {}", ctx.channel());
                    }
                });
    }
    
    private void handlePacket(ChannelHandlerContext ctx, JdwpPacket packet) {
        log.debug("[DebugProxyHandleMsg] {} handling packet: id={}, flags={}, commandSet={}, command={}, errorCode={}",
            connectionType,
            packet.getHeader().getId(),
            packet.getHeader().getFlags(),
            packet.getHeader().getCommandSet(),
            packet.getHeader().getCommand(),
            packet.getHeader().getErrorCode());

        switch (connectionType) {
            case JVM_SERVER:
                debuggerService.handleJvmServerPacket(packet);
                break;
            case DEBUGGER_PROXY:
                debuggerService.handleDebuggerProxyPacket(ctx.channel(), packet);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("[DebugProxyHandleInactive] {} channel inactive: {}", connectionType, ctx.channel());
        debuggerService.handleDisconnect(ctx.channel(), connectionType);
    }
}