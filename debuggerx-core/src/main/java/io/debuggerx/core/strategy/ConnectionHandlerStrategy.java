package io.debuggerx.core.strategy;

import io.debuggerx.core.service.DebuggerService;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * Strategy pattern for handling different connection types (JVM server vs. debugger client).
 * Each strategy implements connection-specific packet routing and processing logic.
 *
 * @author wuou
 */
public interface ConnectionHandlerStrategy {
    /**
     * Handles a JDWP packet for a specific connection type.
     *
     * @param ctx the Netty channel context
     * @param packet the JDWP packet to process
     * @param service the debugger service instance for packet routing
     */
    void handle(ChannelHandlerContext ctx, JdwpPacket packet, DebuggerService service);
}
