package io.debuggerx.core.service;

import io.debuggerx.common.constants.JdwpConstants;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.packet.BreakpointInfo;
import io.debuggerx.protocol.packet.JdwpHeader;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for resolving breakpoint raw JDWP IDs to human-readable names
 * @author ouwu
 */
@Slf4j
public class BreakpointResolver {
    private static final AtomicInteger packetIdGenerator = new AtomicInteger(100000);

    /**
     * Asynchronously resolve breakpoint information
     * Sends JDWP queries to get className, methodName, and lineNumber
     */
    public static void resolveBreakpoint(BreakpointInfo breakpointInfo) {
        try {
            DebugSession session = SessionManager.getInstance().findJvmServerSession();
            Channel jvmChannel = session.getJvmServerChannel();

            if (!jvmChannel.isActive()) {
                log.warn("[BreakpointResolver] JVM channel not active, skipping resolution");
                return;
            }

            // Send ReferenceType.Signature query to get class name
            sendReferenceTypeSignatureQuery(breakpointInfo, jvmChannel, session);

            // Send Method query to get method info and line table
            sendMethodLineTableQuery(breakpointInfo, jvmChannel, session);

        } catch (Exception e) {
            log.error("[BreakpointResolver] Failed to resolve breakpoint requestId={}: {}",
                breakpointInfo.getRequestId(), e.getMessage(), e);
        }
    }


    /**
     * Send ReferenceType.Signature command to get class signature
     */
    private static void sendReferenceTypeSignatureQuery(BreakpointInfo breakpointInfo, Channel jvmChannel, DebugSession session) {
        try {
            int packetId = packetIdGenerator.incrementAndGet();

            // Build JDWP packet: ReferenceType.Signature (CommandSet=2, Command=1)
            // Data: referenceTypeID (classId)
            IdSizes idSizes = SessionManager.getInstance().getIdSizes();
            int referenceTypeIdSize = idSizes.getReferenceTypeIdSize();

            ByteBuffer buffer = ByteBuffer.allocate(referenceTypeIdSize);
            writeId(buffer, breakpointInfo.getClassId(), referenceTypeIdSize);

            JdwpHeader header = new JdwpHeader();
            header.setId(packetId);
            header.setFlags(JdwpConstants.FLAG_COMMAND);
            header.setCommandSet(JdwpConstants.REFERENCE_TYPE_COMMAND_SET);
            header.setCommand(JdwpConstants.REFERENCE_TYPE_SIGNATURE_COMMAND);

            JdwpPacket packet = new JdwpPacket(header, buffer.array());

            // Store mapping so reply processor can find the breakpoint
            session.getPendingResolutions().put(packetId, breakpointInfo.getRequestId());

            // Register packet in packetMap so mapResponseCommand can find it
            session.getPacketMap().put(packetId, packet);

            jvmChannel.writeAndFlush(packet);
            log.debug("[BreakpointResolver] Sent ReferenceType.Signature query for breakpoint requestId={} classId={} packetId={}",
                breakpointInfo.getRequestId(), breakpointInfo.getClassId(), packetId);

        } catch (Exception e) {
            log.error("[BreakpointResolver] Failed to send ReferenceType.Signature query: {}", e.getMessage(), e);
        }
    }

    /**
     * Send Method.LineTable command to get line number mapping
     */
    private static void sendMethodLineTableQuery(BreakpointInfo breakpointInfo, Channel jvmChannel, DebugSession session) {
        try {
            int packetId = packetIdGenerator.incrementAndGet();

            // Build JDWP packet: Method.LineTable (CommandSet=6, Command=1)
            // Data: refType (classId) + methodID
            IdSizes idSizes = SessionManager.getInstance().getIdSizes();
            int referenceTypeIdSize = idSizes.getReferenceTypeIdSize();
            int methodIdSize = idSizes.getMethodIdSize();

            ByteBuffer buffer = ByteBuffer.allocate(referenceTypeIdSize + methodIdSize);
            writeId(buffer, breakpointInfo.getClassId(), referenceTypeIdSize);
            writeId(buffer, breakpointInfo.getMethodId(), methodIdSize);

            JdwpHeader header = new JdwpHeader();
            header.setId(packetId);
            header.setFlags(JdwpConstants.FLAG_COMMAND);
            header.setCommandSet(JdwpConstants.METHOD_COMMAND_SET);
            header.setCommand(JdwpConstants.METHOD_LINE_TABLE_COMMAND);

            JdwpPacket packet = new JdwpPacket(header, buffer.array());

            // Store mapping so reply processor can find the breakpoint
            session.getPendingResolutions().put(packetId, breakpointInfo.getRequestId());

            // Register packet in packetMap so mapResponseCommand can find it
            session.getPacketMap().put(packetId, packet);

            jvmChannel.writeAndFlush(packet);
            log.debug("[BreakpointResolver] Sent Method.LineTable query for breakpoint requestId={} classId={} methodId={} packetId={}",
                breakpointInfo.getRequestId(), breakpointInfo.getClassId(), breakpointInfo.getMethodId(), packetId);

        } catch (Exception e) {
            log.error("[BreakpointResolver] Failed to send Method.LineTable query: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper to write an ID with variable size
     */
    private static void writeId(ByteBuffer buffer, long id, int size) {
        switch (size) {
            case 4:
                buffer.putInt((int) id);
                break;
            case 8:
                buffer.putLong(id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported ID size: " + size);
        }
    }
}
