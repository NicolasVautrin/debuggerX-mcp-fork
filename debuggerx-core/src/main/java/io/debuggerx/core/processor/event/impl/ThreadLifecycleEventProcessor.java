package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.ThreadId;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * 线程生命周期事件处理器
 *
 * @author ouwu
 */
public class ThreadLifecycleEventProcessor implements EventProcessor {
    @Override
    public boolean supports(EventKind eventKind) {
        return eventKind == EventKind.VM_START ||
                eventKind == EventKind.THREAD_START ||
                eventKind == EventKind.THREAD_DEATH;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes, EventKind eventKind) {
        //int	requestID	Request that generated event (or 0 if this event is automatically generated.
        int requestId = buffer.getInt();
        //threadID	thread  Initial thread
        ThreadId.read(buffer, idSizes);
        return Collections.singletonList(requestId);
    }
}
