package io.debuggerx.transport.codec;

import io.debuggerx.common.constants.JdwpConstants;
import io.debuggerx.protocol.packet.JdwpHeader;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 解密数据包
 *
 * @author ouwu
 */
@Slf4j
public class JdwpPacketDecoder extends ByteToMessageDecoder {

    private boolean handshakeCompleted = false;
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!handshakeCompleted) {
            // handshake
            if (in.readableBytes() >= JdwpConstants.HANDSHAKE_BYTES) {
                byte[] handshake = new byte[JdwpConstants.HANDSHAKE_BYTES];
                in.readBytes(handshake);
                String handshakeStr = new String(handshake);
                log.debug("[DecodeHandShake] Received bytes before handshake: {}", handshakeStr);
                
                if (handshakeStr.equals(JdwpConstants.HANDSHAKE_STRING)) {
                    handshakeCompleted = true;
                    log.info("[DecodeHandShake] Handshake completed for channel: {}", ctx.channel());
                    out.add(handshake);
                }
            }
            return;
        }
        
        // 打印数据包解码过程
        if (in.readableBytes() > 0) {
            log.debug("[DecodeHandle] Decoding packet, readable bytes: {}", in.readableBytes());
        }
        
        if (in.readableBytes() < JdwpConstants.HEADER_LENGTH) {
            return;
        }
        
        in.markReaderIndex();
        
        // 读取包头
        byte[] headerBytes = new byte[JdwpConstants.HEADER_LENGTH];
        in.readBytes(headerBytes);
        JdwpHeader header = JdwpHeader.fromBytes(headerBytes);

        // 检查数据包完整性
        int dataLength = header.getLength() - JdwpConstants.HEADER_LENGTH;
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        
        // 读取数据部分
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        log.debug("[DecodeHandle] Decoded data length: {}", data.length);
        
        out.add(new JdwpPacket(header, data));
    }
}