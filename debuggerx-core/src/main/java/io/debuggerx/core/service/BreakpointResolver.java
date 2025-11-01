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
 * Resolves breakpoint raw JDWP IDs (classId, methodId, codeIndex) to human-readable names.
 * Sends asynchronous JDWP queries (ReferenceType.Signature, Method.LineTable) to populate
 * className, methodName, and lineNumber fields for breakpoint tracking.
 *
 * @author ouwu
 */
@Slf4j
public class BreakpointResolver {
    private static final AtomicInteger packetIdGenerator = new AtomicInteger(100000);

    /**
     * Asynchronously resolves breakpoint metadata via JDWP queries.
     * Sends ReferenceType.Signature and Method.LineTable commands to populate human-readable fields.
     *
     * @param breakpointInfo the breakpoint to resolve (updated asynchronously when replies arrive)
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
     * Sends ReferenceType.Signature JDWP command to retrieve the class name.
     *
     * @param breakpointInfo the breakpoint being resolved
     * @param jvmChannel the JVM connection channel
     * @param session the debug session for tracking pending queries
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
     * Sends Method.LineTable JDWP command to retrieve line number mappings.
     *
     * @param breakpointInfo the breakpoint being resolved
     * @param jvmChannel the JVM connection channel
     * @param session the debug session for tracking pending queries
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
     * Writes a JDWP ID (4 or 8 bytes) to a buffer based on JVM's ID size configuration.
     *
     * @param buffer the buffer to write to
     * @param id the ID value
     * @param size the ID size (4 or 8 bytes)
     * @throws IllegalArgumentException if size is not 4 or 8
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
