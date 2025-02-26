package io.debuggerx.protocol.packet;

import io.debuggerx.common.constants.JdwpConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * JDWP协议header
 * @author ouwu
 */
@Data
@Slf4j
public class JdwpHeader {
    /**
     * 数据包总长度 (4 bytes)
     * 包括header和data的总长度
     */
    private int length;
    
    /**
     * 数据包ID (4 bytes)
     * 用于匹配请求和响应
     */
    private int id;
    
    /**
     * 标志位 (1 byte)
     * 0x00: 命令包
     * 0x80: 回复包
     */
    private byte flags;
    
    /**
     * 命令集ID (1 byte, 仅命令包)
     */
    private Byte commandSet;
    
    /**
     * 命令ID (1 byte, 仅命令包)
     */
    private Byte command;
    
    /**
     * 错误码 (2 bytes, 仅回复包)
     */
    private Short errorCode;
    
    public static JdwpHeader fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        JdwpHeader header = new JdwpHeader();
        
        // 读取公共部分
        header.length = buffer.getInt();
        header.id = buffer.getInt();
        header.flags = buffer.get();
        
        if (header.isCommand()) {
            // 命令包: commandSet(1) + command(1)
            header.commandSet = buffer.get();
            header.command = buffer.get();
        } else {
            // 回复包: errorCode(2)
            header.errorCode = buffer.getShort();
        }
        
        return header;
    }
    
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(11);
        if (isCommand()) {
            // 命令包: 4 + 4 + 1 + 1 + 1 = 11 bytes
            buffer.putInt(length);
            buffer.putInt(id);
            buffer.put(flags);
            buffer.put(commandSet);
            buffer.put(command);
        } else {
            // 回复包: 4 + 4 + 1 + 2 = 11 bytes
            buffer.putInt(length);
            buffer.putInt(id);
            buffer.put(flags);
            buffer.putShort(errorCode);
        }
        
        return buffer.array();
    }

    /**
     * 判断是否命令包
     * @return true: 命令包; false: 回复包
     */
    public boolean isCommand() {
        return (flags & JdwpConstants.FLAG_REPLY_PACKET) == 0;
    }

    /**
     * debugger是否释放命令包
     * @return true: 是释放命令包; false: 不是释放命令包
     */
    public boolean isDisposeCommand() {
        return this.getCommandSet() == 1 && this.getCommand() == 6 && this.isCommand();
    }
}