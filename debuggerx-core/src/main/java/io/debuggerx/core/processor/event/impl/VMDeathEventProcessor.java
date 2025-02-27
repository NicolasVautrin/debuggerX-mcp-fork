package io.debuggerx.core.processor.event.impl;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * 虚拟机终止事件处理器
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
