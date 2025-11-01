package io.debuggerx.transport.codec;

import io.debuggerx.common.constants.JdwpConstants;
import io.debuggerx.protocol.packet.JdwpHeader;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 解密数据包
 *
 * @author ouwu
 */
@Slf4j
public class JdwpPacketDecoder extends ByteToMessageDecoder {

    // Separate logger for JDWP packets only, configured programmatically
    private static final Logger packetLog = io.debuggerx.common.logging.PacketLogger.getLogger();

    private boolean handshakeCompleted = false;

    /**
     * Log packet data in readable hexdump format (hex + ASCII)
     * Limited to first 128 bytes to avoid log bloat
     */
    private void logPacketData(byte[] data) {
        int maxBytes = Math.min(data.length, 128);
        StringBuilder sb = new StringBuilder("\n");

        for (int i = 0; i < maxBytes; i += 16) {
            // Offset
            sb.append(String.format("  %04X: ", i));

            // Hex bytes
            for (int j = 0; j < 16; j++) {
                if (i + j < maxBytes) {
                    sb.append(String.format("%02X ", data[i + j]));
                } else {
                    sb.append("   ");
                }
                if (j == 7) sb.append(" "); // Space in middle
            }

            sb.append(" |");

            // ASCII representation
            for (int j = 0; j < 16 && i + j < maxBytes; j++) {
                byte b = data[i + j];
                if (b >= 32 && b < 127) {
                    sb.append((char) b);
                } else {
                    sb.append('.');
                }
            }

            sb.append("|\n");
        }

        if (data.length > maxBytes) {
            sb.append(String.format("  ... (%d more bytes)\n", data.length - maxBytes));
        }

        packetLog.info(sb.toString());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!handshakeCompleted) {
            // handshake
            if (in.readableBytes() >= JdwpConstants.HANDSHAKE_BYTES) {
                byte[] handshake = new byte[JdwpConstants.HANDSHAKE_BYTES];
                in.readBytes(handshake);
                String handshakeStr = new String(handshake);

                if (handshakeStr.equals(JdwpConstants.HANDSHAKE_STRING)) {
                    handshakeCompleted = true;
                    log.info("[DecodeHandShake] Handshake completed for channel: {}", ctx.channel());
                    out.add(handshake);
                }
            }
            return;
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

        JdwpPacket packet = new JdwpPacket(header, data);

        // Log ALL packets to separate packets log with formatted data content
        if (header.isCommand()) {
            packetLog.info("COMMAND id={} commandSet={} command={} dataLen={}",
                header.getId(), header.getCommandSet(), header.getCommand(), dataLength);
            if (dataLength > 0) {
                logPacketData(data);
            }
        } else {
            packetLog.info("REPLY id={} flags={} errorCode={} dataLen={}",
                header.getId(), header.getFlags(), header.getErrorCode(), dataLength);
            if (dataLength > 0) {
                logPacketData(data);
            }
        }

        out.add(packet);
    }
}