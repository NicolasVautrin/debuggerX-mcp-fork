package io.debuggerx.core.processor.event.impl;

import io.debuggerx.common.utils.ByteBufferUtils;
import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.ReferenceTypeId;
import io.debuggerx.protocol.jdwp.ThreadId;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * 类加载事件处理器
 *
 * @author ouwu
 */
public class ClassLifecycleEventProcessor implements EventProcessor {
    private EventKind eventKind;
    @Override
    public boolean supports(EventKind eventKind) {
        this.eventKind = eventKind;
        return eventKind == EventKind.CLASS_PREPARE || 
               eventKind == EventKind.CLASS_UNLOAD;
    }

    @Override
    public List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes) {
        int requestId = buffer.getInt();
        
        if (eventKind == EventKind.CLASS_PREPARE) {
            ThreadId.read(buffer, idSizes);
            buffer.get();
            ReferenceTypeId.read(buffer, idSizes);
            ByteBufferUtils.getString(buffer);
            buffer.getInt();
        } else {
            ByteBufferUtils.getString(buffer);
        }
        return Collections.singletonList(requestId);
    }
}
