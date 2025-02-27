package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.Location;
import io.debuggerx.protocol.jdwp.ThreadId;
import io.debuggerx.protocol.jdwp.Value;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * 方法返回值事件处理器
 *
 * @author ouwu
 */
public class ReturnValueEventProcessor implements EventProcessor {
    @Override
    public boolean supports(EventKind eventKind) {
        return eventKind == EventKind.METHOD_EXIT_WITH_RETURN_VALUE;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes, EventKind eventKind) {
        int requestId = buffer.getInt();
        ThreadId.read(buffer, idSizes);
        Location.read(buffer, idSizes);
        Value.read(buffer, idSizes);
        return Collections.singletonList(requestId);
    }
}
