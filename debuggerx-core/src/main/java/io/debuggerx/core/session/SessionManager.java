package io.debuggerx.core.session;

import io.debuggerx.common.exception.DebuggerException;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages debug sessions across all JVM connections.
 * Implements singleton pattern to provide global access to session state.
 * Handles session lifecycle (creation, lookup, removal) and ID size configuration.
 *
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
     * Creates a new debug session for a JVM server connection.
     *
     * @param jvmServerChannel the channel connected to the JVM
     * @return the newly created debug session
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
     * Finds the active JVM server debug session.
     *
     * @return the active debug session
     * @throws DebuggerException if no session exists
     */
    public DebugSession findJvmServerSession() {
        return sessions.values().stream().findAny().orElseThrow(() -> new DebuggerException("No available jvm server session found"));
    }

    /**
     * Closes all debug sessions gracefully (used during proxy shutdown).
     * Disconnects all debugger clients and JVM connections.
     */
    public void closeAllSessions() {
        log.info("[SessionManager] Closing all sessions ({} active)", sessions.size());
        sessions.forEach((channel, session) -> {
            try {
                // Close all debugger connections
                session.getDebuggerChannels().values().forEach(debuggerChannel -> {
                    if (debuggerChannel.isActive()) {
                        debuggerChannel.close();
                    }
                });

                // Close JVM server channel
                if (channel.isActive()) {
                    channel.close();
                }

                log.info("[SessionManager] Closed session: {}", session.getSessionId());
            } catch (Exception e) {
                log.error("[SessionManager] Error closing session: {}", e.getMessage(), e);
            }
        });
        sessions.clear();
        log.info("[SessionManager] All sessions closed");
    }

}