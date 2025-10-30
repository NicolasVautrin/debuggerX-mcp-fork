package io.debuggerx.protocol.packet;

import lombok.Data;

/**
 * Breakpoint information tracked globally by debuggerX
 * @author ouwu
 */
@Data
public class BreakpointInfo {
    /**
     * Request ID returned by JVM
     */
    private final int requestId;

    /**
     * Raw JDWP Location data - typeTag (1=class, 2=interface, 3=array)
     */
    private final byte typeTag;

    /**
     * Raw JDWP Location data - reference type ID (class/interface)
     */
    private final long classId;

    /**
     * Raw JDWP Location data - method ID
     */
    private final long methodId;

    /**
     * Raw JDWP Location data - bytecode index within the method
     */
    private final long codeIndex;

    /**
     * Source channel that created this breakpoint
     */
    private final PacketSource source;

    /**
     * Timestamp when breakpoint was created
     */
    private final long createdAt;

    /**
     * Resolved class name (null if not resolved yet)
     */
    private String className;

    /**
     * Resolved method name (null if not resolved yet)
     */
    private String methodName;

    /**
     * Resolved line number (-1 if not resolved yet)
     */
    private int lineNumber;

    public BreakpointInfo(int requestId, byte typeTag, long classId, long methodId, long codeIndex, PacketSource source) {
        this.requestId = requestId;
        this.typeTag = typeTag;
        this.classId = classId;
        this.methodId = methodId;
        this.codeIndex = codeIndex;
        this.source = source;
        this.createdAt = System.currentTimeMillis();
        this.className = null;
        this.methodName = null;
        this.lineNumber = -1;
    }

    public void setResolvedInfo(String className, String methodName, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }
}
