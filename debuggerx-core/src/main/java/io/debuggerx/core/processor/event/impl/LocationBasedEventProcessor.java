package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.Location;
import io.debuggerx.protocol.jdwp.ThreadId;
import io.debuggerx.protocol.packet.BreakpointEventInfo;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Processes location-based JDWP events (breakpoint, step, method entry/exit).
 * For BREAKPOINT events, captures thread context and updates session state for inspection.
 *
 * @author ouwu
 */
@Slf4j
public class LocationBasedEventProcessor implements EventProcessor {
    @Override
    public boolean supports(EventKind eventKind) {
        return eventKind == EventKind.SINGLE_STEP ||
                eventKind == EventKind.BREAKPOINT ||
                eventKind == EventKind.METHOD_ENTRY ||
                eventKind == EventKind.METHOD_EXIT;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes, EventKind eventKind) {
        int requestId = buffer.getInt();
        //threadID	thread	Stepped thread
        //location	location	Location stepped to
        ThreadId threadId = ThreadId.read(buffer, idSizes);
        Location location = Location.read(buffer, idSizes);

        // For BREAKPOINT events, capture threadId and reference existing breakpoint (non-intrusive observer)
        if (eventKind == EventKind.BREAKPOINT) {
            try {
                DebugSession session = SessionManager.getInstance().findJvmServerSession();
                if (session != null) {
                    io.debuggerx.protocol.packet.BreakpointInfo breakpoint = session.getGlobalBreakpoints().get(requestId);
                    if (breakpoint != null) {
                        BreakpointEventInfo eventInfo = new BreakpointEventInfo();
                        eventInfo.setThreadId(threadId.asLong());
                        eventInfo.setBreakpoint(breakpoint);

                        session.setCurrentBreakpointEvent(eventInfo);
                    }
                }
            } catch (Exception e) {
                log.error("[LocationBasedEventProcessor] Failed to process BREAKPOINT event: {}", e.getMessage(), e);
            }
        }

        return Collections.singletonList(requestId);
    }
}
