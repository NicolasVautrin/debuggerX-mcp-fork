package io.debuggerx.core.service;

import io.debuggerx.common.enums.ConnectionType;
import io.debuggerx.common.utils.ChannelUtils;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * 调试服务
 * 处理调试目标与调试器之间的通信
 * @author ouwu
 */
@Slf4j
public class DebuggerService {

    private static volatile DebuggerService instance;
    private final SessionManager sessionManager = SessionManager.getInstance();

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
    
    public void handleJvmServerPacket(JdwpPacket packet) {
        DebugSession session = sessionManager.findJvmServerSession();
        if (session == null) {
            log.error("No session found for jvm server session. Is command packet?:{}", packet.getHeader().isCommand());
            return;
        }
        // 根据包全局ID取到原始包ID，塞入原包进行回复
        int newId = packet.getHeader().getId();
        int originId = session.getOriginIdByNewId(packet.getHeader().getId());
        packet.getHeader().setId(originId);
        log.info("[JvmServer] Replay packet to debugger.OriginId:{},NewId:{}", originId, newId);
        // 广播给所有连接的调试器
        session.broadcast(packet);
    }
    
    public void handleDebuggerProxyPacket(Channel channel, JdwpPacket packet) {
        if (packet.getHeader().isDisposeCommand()) {
            log.info("[Dispose command] Dispose command received, closing debugger channel: {}", channel);
            channel.close();
            return;
        }
        // 查找对应的调试目标会话并转发
        DebugSession session = sessionManager.findJvmServerSession();
        // 检查channel状态
        if (session.getJvmServerChannel().isActive()) {
            // 根据原始包ID 取全局唯一ID塞入包中并保存关联关系
            int originId = packet.getHeader().getId();
            int newId = session.getNewIdAndSaveOriginLink(packet.getHeader().getId());
            packet.getHeader().setId(newId);
            log.info("[ProxyDebugger] Sending packet to jvm server.OriginId:{},NewId:{}", originId, newId);
            ChannelFuture future = session.getJvmServerChannel().writeAndFlush(packet);
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    log.error("[JvmServer]Failed to forward packet to jvm server", f.cause());
                }
            });
        }
    }
    
    public void handleHandshake(Channel channel, ConnectionType connectionType) {
        switch (connectionType) {
            case JVM_SERVER:
                DebugSession session = sessionManager.createJvmServerSession(channel);
                session.setHandshakeCompleted(true);
                log.info("[JvmServerHandShake] Jvm server handshake completed: {}", session.getSessionId());
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
} 