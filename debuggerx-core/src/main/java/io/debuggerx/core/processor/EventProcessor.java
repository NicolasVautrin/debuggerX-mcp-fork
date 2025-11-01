package io.debuggerx.core.processor;

import io.debuggerx.common.exception.DebuggerException;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Processes JDWP events and extracts request IDs for routing to debugger clients.
 * Each implementation handles specific event types (breakpoint, step, exception, etc.).
 *
 * @author wuou
 */
public interface EventProcessor {

    /**
     * Determines if this processor handles the given event type.
     *
     * @param eventKind the JDWP event type
     * @return true if this processor handles the event, false otherwise
     */
    boolean supports(EventKind eventKind);

    /**
     * Processes a JDWP event and extracts associated request IDs.
     *
     * @param buffer the data buffer containing event payload
     * @param idSizes the JVM's ID size configuration
     * @param eventKind the JDWP event type being processed
     * @return list of request IDs extracted from the event
     * @throws DebuggerException if event processing fails
     */
    List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes, EventKind eventKind) throws DebuggerException;
}
