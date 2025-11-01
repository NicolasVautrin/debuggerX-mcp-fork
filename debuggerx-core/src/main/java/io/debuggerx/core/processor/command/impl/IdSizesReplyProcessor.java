package io.debuggerx.core.processor.command.impl;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.packet.JdwpPacket;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Processes VirtualMachine.IDSizes reply to configure JVM ID size constants.
 * Stores fieldID, methodID, objectID, referenceTypeID, and frameID sizes globally for packet parsing.
 *
 * @author ouwu
 */
public class IdSizesReplyProcessor implements CommandProcessor {

    @Override
    public List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet) {
        this.handleIdSizes(packet);
        return null;
    }

    public void handleIdSizes(JdwpPacket packet) {
        IdSizes read = IdSizes.read(ByteBuffer.wrap(packet.getData()));
        SessionManager.getInstance().setIdSizes(read);
    }
}
