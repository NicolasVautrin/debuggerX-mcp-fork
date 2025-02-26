package io.debuggerx.core.processor.registry;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.processor.command.impl.ClearEventRequestCommandProcessor;
import io.debuggerx.core.processor.command.impl.CompositeEventCommandProcessor;
import io.debuggerx.core.processor.command.impl.IdSizesReplyProcessor;
import io.debuggerx.core.processor.command.impl.SetEventRequestReplyProcessor;
import io.debuggerx.protocol.enums.CommandIdentifier;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author ouwu
 */
public class CommandProcessorRegistry {
    private final Map<CommandIdentifier, CommandProcessor> processorMap = new EnumMap<>(CommandIdentifier.class);

    public CommandProcessorRegistry(EventProcessorRegistry eventProcessors) {
        register(CommandIdentifier.ID_SIZES_REPLY, new IdSizesReplyProcessor());
        register(CommandIdentifier.SET_EVENT_REQUEST_REPLY, new SetEventRequestReplyProcessor());
        register(CommandIdentifier.CLEAR_EVENT_REQUEST_COMMAND, new ClearEventRequestCommandProcessor());
        register(CommandIdentifier.COMPOSITE_EVENT_COMMAND, new CompositeEventCommandProcessor(eventProcessors));
    }

    public void register(CommandIdentifier id, CommandProcessor processor) {
        processorMap.put(id, processor);
    }
    
    public CommandProcessor getProcessor(CommandIdentifier id) {
        return processorMap.get(id);
    }
}