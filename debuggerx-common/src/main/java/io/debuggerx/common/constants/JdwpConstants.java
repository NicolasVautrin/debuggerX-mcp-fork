package io.debuggerx.common.constants;

/**
 * JDWP协议常量
 *
 * @author ouwu
 */
public class JdwpConstants {
    private JdwpConstants() {
    }

    /**
     * JDWP握手包长度
     */
    public static final int HANDSHAKE_BYTES = 14;
    /**
     * JDWP握手包字符串
     */
    public static final String HANDSHAKE_STRING = "JDWP-Handshake";
    /**
     * JDWP头部长度
     */
    public static final int HEADER_LENGTH = 11;
    /**
     * JDWP握手包
     */
    public static final byte[] HANDSHAKE_PACKET = new byte[] {
        'J', 'D', 'W', 'P', '-', 'H', 'a', 'n', 'd', 's', 'h', 'a', 'k', 'e'
    };
    /**
     * JDWP回复包标志
     */
    public static final byte FLAG_REPLY_PACKET = (byte) 0x80;
    /**
     * 命令包标志
     */
    public static final byte FLAG_COMMAND = (byte) 0;

    /**
     * EVENT_REQUEST_COMMAND_SET
     */
    public static final byte EVENT_REQUEST_COMMAND_SET = (byte) 15;
    /**
     * CLEAR_BREAK_POINT_COMMAND
     */
    public static final byte CLEAR_BREAK_POINT_COMMAND = (byte) 2;

    /**
     * ReferenceType command set
     */
    public static final byte REFERENCE_TYPE_COMMAND_SET = (byte) 2;
    /**
     * ReferenceType.Signature command
     */
    public static final byte REFERENCE_TYPE_SIGNATURE_COMMAND = (byte) 1;
    /**
     * ReferenceType.Methods command
     */
    public static final byte REFERENCE_TYPE_METHODS_COMMAND = (byte) 5;

    /**
     * Method command set
     */
    public static final byte METHOD_COMMAND_SET = (byte) 6;
    /**
     * Method.LineTable command
     */
    public static final byte METHOD_LINE_TABLE_COMMAND = (byte) 1;


}