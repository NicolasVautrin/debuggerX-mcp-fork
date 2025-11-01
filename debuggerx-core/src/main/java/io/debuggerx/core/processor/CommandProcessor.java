package io.debuggerx.core.processor;

import io.debuggerx.protocol.packet.JdwpPacket;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Processes JDWP command packets and extracts request IDs for event tracking.
 * Implementations handle specific command types and parse their payloads.
 *
 * @author ouwu
 */
public interface CommandProcessor {
    /**
     * Processes a JDWP command packet and extracts associated request IDs.
     *
     * @param byteBuffer the data buffer containing command payload
     * @param packet the JDWP packet being processed
     * @return list of request IDs extracted from the command, or empty list if none
     */
    List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet);
}