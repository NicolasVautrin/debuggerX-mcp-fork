package io.debuggerx.core.processor.command.impl;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.packet.JdwpPacket;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * @author ouwu
 */
public class SetEventRequestReplyProcessor implements CommandProcessor {

    @Override
    public List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet) {
        return Collections.singletonList(byteBuffer.getInt());
    }
}
