package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.*;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * 字段访问事件处理器
 *
 * @author ouwu
 */
public class FieldAccessEventProcessor implements EventProcessor {
    @Override
    public boolean supports(EventKind eventKind) {
        return eventKind == EventKind.FIELD_ACCESS ||
               eventKind == EventKind.FIELD_MODIFICATION;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes, EventKind eventKind) {
        int requestId = buffer.getInt();
        ThreadId.read(buffer, idSizes);
        Location.read(buffer, idSizes);
        buffer.get();
        ReferenceTypeId.read(buffer, idSizes);
        FieldId.read(buffer, idSizes);
        TaggedObjectId.read(buffer, idSizes);
        
        if (eventKind == EventKind.FIELD_MODIFICATION) {
            Value.read(buffer, idSizes);
        }
        return Collections.singletonList(requestId);
    }
}
