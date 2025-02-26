package io.debuggerx.core.strategy;

import io.debuggerx.core.service.DebuggerService;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author wuou
 */
public interface ConnectionHandlerStrategy {
    /**
     * 处理特定类型连接的数据包
     *
     * @param ctx Netty上下文
     * @param packet JDWP数据包
     * @param service 调试服务实例
     */
    void handle(ChannelHandlerContext ctx, JdwpPacket packet, DebuggerService service);
}
