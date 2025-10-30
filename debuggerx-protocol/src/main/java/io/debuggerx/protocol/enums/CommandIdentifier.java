package io.debuggerx.protocol.enums;

import io.debuggerx.protocol.packet.JdwpHeader;

import java.util.Arrays;

import static io.debuggerx.protocol.enums.CommandType.COMMAND;
import static io.debuggerx.protocol.enums.CommandType.REPLY;

/**
 * 命令包标识
 *
 * @author ouwu
 */
public enum CommandIdentifier {

    /**
     * 释放命令包
     */
    DISPOSE_COMMAND(1, 6, COMMAND),

    /**
     * 动态获取目标虚拟机（JVM）运行时关键标识符的字节长度
     */
    ID_SIZES_REPLY(1, 7, REPLY),

    /**
     * 设置事件回复
     * int	requestID	ID of created request
     */
    SET_EVENT_REQUEST_REPLY(15, 1, REPLY),

    /**
     * 设置事件命令
     */
    SET_EVENT_REQUEST_COMMAND(15, 1, COMMAND),

    /**
     * 清除事件命令
     * Out Data
     * byte	eventKind	Event kind to clear
     * int	requestID	ID of request to clear
     */
    CLEAR_EVENT_REQUEST_COMMAND(15, 2, COMMAND),

    /**
     * 清除事件命令回复
     * Reply Data
     * (None)
     */
    CLEAR_EVENT_REQUEST_REPLY(15, 2, REPLY),

    /**
     * 复合事件命令
     */
    COMPOSITE_EVENT_COMMAND(64, 100, COMMAND),

    /**
     * ReferenceType.Signature reply - returns class signature
     */
    REFERENCE_TYPE_SIGNATURE_REPLY(2, 1, REPLY),

    /**
     * Method.LineTable reply - returns line number table for a method
     */
    METHOD_LINE_TABLE_REPLY(6, 1, REPLY);

    private final short commandSetId;
    private final short commandId;
    private final CommandType type;

    CommandIdentifier(int commandSetId, int commandId, CommandType type) {
        this.commandSetId = (short) commandSetId;
        this.commandId = (short) commandId;
        this.type = type;
    }

    public static CommandIdentifier of(JdwpHeader header) {
        return Arrays.stream(values())
                .filter(ci -> ci.getCommandSetId() == header.getCommandSet())
                .filter(ci -> ci.getCommandId() == header.getCommand())
                .filter(ci -> header.isCommand() ? ci.getType().equals(COMMAND) : ci.getType().equals(REPLY))
                .findFirst().orElse(null);
    }

    public short getCommandSetId() {
        return commandSetId;
    }

    public short getCommandId() {
        return commandId;
    }

    public CommandType getType() {
        return type;
    }

    public short getFlags() {
        if (type == REPLY) {
            return -128;
        } else {
            return 0;
        }
    }
}
