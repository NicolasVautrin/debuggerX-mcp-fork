package io.debuggerx.protocol.packet;

import io.debuggerx.common.constants.JdwpConstants;
import lombok.Data;

import java.util.List;

/**
 * JDWP协议数据包
 *
 * @author ouwu
 */
@Data
public class JdwpPacket {
    private JdwpHeader header;
    private byte[] data;
    private List<Integer> requestIds;
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

    public static JdwpPacket createClearAllBreakpointsPacket() {
        JdwpHeader jdwpHeader = new JdwpHeader();
        jdwpHeader.setId(888);
        jdwpHeader.setLength(JdwpConstants.HEADER_LENGTH);
        jdwpHeader.setCommandSet(JdwpConstants.EVENT_REQUEST_COMMAND_SET);
        jdwpHeader.setCommand(JdwpConstants.CLEAR_ALL_BREAK_POINT_COMMAND);
        jdwpHeader.setFlags(JdwpConstants.FLAG_COMMAND);
        return new JdwpPacket(jdwpHeader, null);
    }
}