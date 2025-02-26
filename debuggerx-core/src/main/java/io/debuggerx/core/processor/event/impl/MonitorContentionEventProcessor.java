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
 * 监视器竞争事件处理器
 * @author ouwu
 */
public class MonitorContentionEventProcessor implements EventProcessor {
    @Override
    public boolean supports(EventKind eventKind) {
        return eventKind == EventKind.MONITOR_CONTENDED_ENTER || 
               eventKind == EventKind.MONITOR_CONTENDED_ENTERED;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes) {
        int requestId = buffer.getInt();
        ThreadId.read(buffer, idSizes);
        TaggedObjectId.read(buffer, idSizes);
        Location.read(buffer, idSizes);
        return Collections.singletonList(requestId);
    }
}
