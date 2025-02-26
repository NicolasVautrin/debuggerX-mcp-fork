package io.debuggerx.core.processor.registry;

import io.debuggerx.core.processor.EventProcessor;
import io.debuggerx.core.processor.event.impl.*;
import io.debuggerx.protocol.enums.EventKind;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * 处理器注册中心
 *
 * @author wuou
 */
public class EventProcessorRegistry {
    private final Map<EventKind, EventProcessor> processorMap = new EnumMap<>(EventKind.class);

    public EventProcessorRegistry() {
        this.registerProcessor(new ClassLifecycleEventProcessor());
        this.registerProcessor(new ExceptionEventProcessor());
        this.registerProcessor(new FieldAccessEventProcessor());
        this.registerProcessor(new LocationBasedEventProcessor());
        this.registerProcessor(new MonitorContentionEventProcessor());
        this.registerProcessor(new MonitorWaitEventProcessor());
        this.registerProcessor(new ReturnValueEventProcessor());
        this.registerProcessor(new ThreadLifecycleEventProcessor());
        this.registerProcessor(new VMDeathEventProcessor());
    }

    public void registerProcessor(EventProcessor processor) {
        for (EventKind kind : EventKind.values()) {
            if (processor.supports(kind)) {
                processorMap.put(kind, processor);
            }
        }
    }

    public Optional<EventProcessor> getProcessor(EventKind kind) {
        return Optional.ofNullable(processorMap.get(kind));
    }

}
