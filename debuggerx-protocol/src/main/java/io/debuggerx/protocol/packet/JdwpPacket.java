package io.debuggerx.protocol.packet;

import lombok.Data;
import io.debuggerx.common.constants.JdwpConstants;

/**
 * JDWP协议数据包
 *
 * @author ouwu
 */
@Data
public class JdwpPacket {
    private JdwpHeader header;
    private byte[] data;
    
    public JdwpPacket(JdwpHeader header, byte[] data) {
        this.header = header;
        this.data = data;
        this.header.setLength(JdwpConstants.HEADER_LENGTH + (data != null ? data.length : 0));
    }
    
    public byte[] toBytes() {
        int totalLength = JdwpConstants.HEADER_LENGTH;
        if (data != null) {
            totalLength += data.length;
        }

        byte[] bytes = new byte[totalLength];
        System.arraycopy(header.toBytes(), 0, bytes, 0, JdwpConstants.HEADER_LENGTH);

        if (data != null) {
            System.arraycopy(data, 0, bytes, JdwpConstants.HEADER_LENGTH, data.length);
        }

        return bytes;
    }
} 