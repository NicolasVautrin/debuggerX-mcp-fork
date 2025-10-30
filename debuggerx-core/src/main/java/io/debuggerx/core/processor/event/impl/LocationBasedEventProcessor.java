package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.Location;
import io.debuggerx.protocol.jdwp.ThreadId;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * @author ouwu
 */
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

        return Collections.singletonList(requestId);
    }
}
