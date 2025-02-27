package io.debuggerx.core.processor.command.impl;

import io.debuggerx.common.exception.DebuggerException;
import io.debuggerx.common.utils.CollectionUtils;
import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.core.processor.registry.EventProcessorRegistry;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.packet.JdwpPacket;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author wuou
 */
@Slf4j
public class CompositeEventCommandProcessor implements CommandProcessor {

    private final EventProcessorRegistry eventProcessors;

    public CompositeEventCommandProcessor(EventProcessorRegistry eventProcessors) {
        this.eventProcessors = eventProcessors;
    }

    @Override
    public List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet) {
        return this.compositeEventHandle(byteBuffer, packet);
    }

    private List<Integer> compositeEventHandle(ByteBuffer byteBuffer, JdwpPacket packet) {
        // suspendPolicy
        byteBuffer.get();
        int events = byteBuffer.getInt();
        List<Integer> requestIds = new ArrayList<>(events);

        for (int i = 0; i < events; i++) {
            byte eventKind = byteBuffer.get();
            EventKind event = EventKind.findByValue(eventKind);
            if (Objects.isNull(event)) {
                log.error("UnKnow EventKind:{}, packet string:{}", eventKind, packet.toString());
                log.error("UnKnow EventKind:{}, packet byte:{}", eventKind, packet.toBytes());
            }

            EventProcessor eventProcessor = eventProcessors.getProcessor(event).orElseThrow(() -> new DebuggerException("UnKnow Event."));
            List<Integer> ids = eventProcessor.processEvent(byteBuffer, SessionManager.getInstance().getIdSizes(), event);
            if (!CollectionUtils.isEmpty(ids)) {
                requestIds.addAll(ids);
            }
        }
        return CollectionUtils.isEmpty(requestIds) ? null : requestIds;
    }
}
