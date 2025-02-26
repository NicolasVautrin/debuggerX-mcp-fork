package io.debuggerx.transport.codec;

import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Encode Handle
 * @author ouwu
 */
@Slf4j
public class JdwpPacketEncoder extends MessageToByteEncoder<JdwpPacket> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, JdwpPacket packet, ByteBuf out) {
        byte[] data = packet.toBytes();
        out.writeBytes(data);
    }
}