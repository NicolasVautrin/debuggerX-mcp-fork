package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Processes VM_DEATH event indicating JVM shutdown.
 * Extracts only the request ID from the event.
 *
 * @author ouwu
 */
public class VMDeathEventProcessor implements EventProcessor {
    @Override
    public boolean supports(EventKind eventKind) {
        return eventKind == EventKind.VM_DEATH;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes, EventKind eventKind) {
        int requestId = buffer.getInt();
        return Collections.singletonList(requestId);
    }
}
