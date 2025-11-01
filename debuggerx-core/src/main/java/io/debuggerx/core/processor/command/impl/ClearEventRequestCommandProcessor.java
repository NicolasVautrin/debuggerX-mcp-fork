package io.debuggerx.core.processor.command.impl;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.protocol.packet.JdwpPacket;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Processes EventRequest.Clear commands to remove event requests.
 * Extracts event kind and request ID from the command payload.
 *
 * @author wuou
 */
public class ClearEventRequestCommandProcessor implements CommandProcessor {
    @Override
    public List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet) {
        // eventKind
        byteBuffer.get();
        return Collections.singletonList(byteBuffer.getInt());
    }
}
