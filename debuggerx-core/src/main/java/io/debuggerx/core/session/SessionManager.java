package io.debuggerx.core.session;

import io.debuggerx.common.exception.DebuggerException;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器
 * @author ouwu
 */
@Slf4j
public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private final Map<Channel, DebugSession> sessions = new ConcurrentHashMap<>();
    @Getter
    private IdSizes idSizes;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setIdSizes(IdSizes idSizes) {
        this.idSizes = idSizes;
    }

    /**
     * 创建jvm服务端调试会话
     */
    public DebugSession createJvmServerSession(Channel jvmServerChannel) {
        DebugSession session = new DebugSession(jvmServerChannel);
        sessions.put(jvmServerChannel, session);
        return session;
    }
    
    public void removeSession(Channel channel) {
        DebugSession session = sessions.remove(channel);
        if (session != null) {
            log.info("[Disconnect] Removed debugger session");
            // 关闭所有相关的调试器连接
            session.getDebuggerChannels().values().forEach(Channel::close);
        }
    }

    /**
     * 找到jvm服务端调试会话
     * @return 找到的会话，如果未找到则返回异常
     */
    public DebugSession findJvmServerSession() {
        return sessions.values().stream().findAny().orElseThrow(() -> new DebuggerException("No available jvm server session found"));
    }
    
}