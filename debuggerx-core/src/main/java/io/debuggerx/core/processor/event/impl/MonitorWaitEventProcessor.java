package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.Location;
import io.debuggerx.protocol.jdwp.TaggedObjectId;
import io.debuggerx.protocol.jdwp.ThreadId;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Processes monitor wait events (MONITOR_WAIT, MONITOR_WAITED).
 * Extracts thread, monitor object, location, and timeout/timed_out flag.
 *
 * @author ouwu
 */
public class MonitorWaitEventProcessor implements EventProcessor {

    @Override
    public boolean supports(EventKind eventKind) {
        return eventKind == EventKind.MONITOR_WAIT ||
               eventKind == EventKind.MONITOR_WAITED;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes, EventKind eventKind) {
        int requestId = buffer.getInt();

        ThreadId.read(buffer, idSizes);
        TaggedObjectId.read(buffer, idSizes);
        Location.read(buffer, idSizes);
        
        // 读取事件特有字段
        if (eventKind == EventKind.MONITOR_WAIT) {
            buffer.getLong();
        } else {
            buffer.get();
        }
        return Collections.singletonList(requestId);
    }
}
