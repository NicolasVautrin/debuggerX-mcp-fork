package io.debuggerx.protocol.packet;

import lombok.Data;

/**
 * Lightweight wrapper for BREAKPOINT event - just thread + breakpoint reference
 * Uses observer pattern to avoid duplicating resolved data
 *
 * @author ouwu
 */
@Data
public class BreakpointEventInfo {
    /**
     * Thread ID that hit the breakpoint
     */
    private long threadId;

    /**
     * Reference to the breakpoint from globalBreakpoints (contains resolved info)
     */
    private BreakpointInfo breakpoint;

    /**
     * Timestamp when event was received
     */
    private long timestamp = System.currentTimeMillis();
}
