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

    /**
     * VirtualMachine command set
     */
    public static final byte VIRTUAL_MACHINE_COMMAND_SET = (byte) 1;
    /**
     * VirtualMachine.ClassesBySignature command
     */
    public static final byte CLASSES_BY_SIGNATURE_COMMAND = (byte) 2;
    /**
     * VirtualMachine.AllClasses command
     */
    public static final byte ALL_CLASSES_COMMAND = (byte) 3;
    /**
     * VirtualMachine.AllThreads command
     */
    public static final byte ALL_THREADS_COMMAND = (byte) 4;

    /**
     * ClassType command set
     */
    public static final byte CLASS_TYPE_COMMAND_SET = (byte) 3;
    /**
     * ClassType.InvokeMethod command
     */
    public static final byte CLASS_TYPE_INVOKE_METHOD_COMMAND = (byte) 3;

    /**
     * ObjectReference command set
     */
    public static final byte OBJECT_REFERENCE_COMMAND_SET = (byte) 9;
    /**
     * ObjectReference.InvokeMethod command
     */
    public static final byte OBJECT_REFERENCE_INVOKE_METHOD_COMMAND = (byte) 6;

    /**
     * ArrayType command set
     */
    public static final byte ARRAY_TYPE_COMMAND_SET = (byte) 4;
    /**
     * ArrayType.NewInstance command
     */
    public static final byte ARRAY_TYPE_NEW_INSTANCE_COMMAND = (byte) 1;

    /**
     * ArrayReference command set
     */
    public static final byte ARRAY_REFERENCE_COMMAND_SET = (byte) 13;
    /**
     * ArrayReference.SetValues command
     */
    public static final byte ARRAY_REFERENCE_SET_VALUES_COMMAND = (byte) 3;

}