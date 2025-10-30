package io.debuggerx.core.processor.command.impl;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.service.BreakpointResolver;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;
import io.debuggerx.protocol.jdwp.Location;
import io.debuggerx.protocol.packet.BreakpointInfo;
import io.debuggerx.protocol.packet.BreakpointRequestRelation;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.debuggerx.protocol.packet.PacketSource;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author ouwu
 */
@Slf4j
public class SetEventRequestReplyProcessor implements CommandProcessor {

    @Override
    public List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet) {
        int requestId = byteBuffer.getInt();
        cacheBreakpointRequestId(requestId, packet);
        return Collections.singletonList(requestId);
    }

    private void cacheBreakpointRequestId(int requestId, JdwpPacket packet) {
        DebugSession session = SessionManager.getInstance().findJvmServerSession();
        Pair<Integer, PacketSource> packetSourcePair = session.getPacketIdMap().get(packet.getHeader().getId());
        // 原命令包channel
        PacketSource source = packetSourcePair.getRight();
        Map<Channel, Set<BreakpointRequestRelation>> breakpointRequestMap = session.getBreakpointRequestMap();
        breakpointRequestMap.computeIfAbsent(source.getChannel(), id -> new CopyOnWriteArraySet<>());
        Set<BreakpointRequestRelation> breakpointRequestRelations = breakpointRequestMap.get(source.getChannel());
        // 找到原命令包EventKind类型
        JdwpPacket originPacket = session.findPacketByNewId(packet.getHeader().getId());
        EventKind eventKind = EventKind.findByValue(originPacket.getData()[0]);
        if (Objects.isNull(eventKind)) {
            log.error("Cache Breakpoint requestId found event kind is null.");
            return;
        }
        BreakpointRequestRelation breakpointRequestRelation = new BreakpointRequestRelation(eventKind, requestId);
        if (breakpointRequestRelations.contains(breakpointRequestRelation)) {
            return;
        }
        breakpointRequestRelations.add(breakpointRequestRelation);

        // Store breakpoint in global registry if it's a BREAKPOINT event
        if (eventKind == EventKind.BREAKPOINT) {
            storeGlobalBreakpoint(requestId, originPacket, source, session);
        }
    }

    /**
     * Extract breakpoint details and store in global registry
     */
    private void storeGlobalBreakpoint(int requestId, JdwpPacket originPacket, PacketSource source, DebugSession session) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(originPacket.getData());
            // Skip eventKind (1 byte) and suspendPolicy (1 byte)
            buffer.get();
            buffer.get();

            // Read modifiers count
            int modifiersCount = buffer.getInt();

            // Get IdSizes to parse Location correctly
            io.debuggerx.protocol.jdwp.IdSizes idSizes = SessionManager.getInstance().getIdSizes();
            if (idSizes == null) {
                log.warn("[GlobalBreakpoint] IdSizes not available yet, skipping breakpoint registration");
                return;
            }

            // Parse modifiers to find LocationOnly (modKind=7)
            for (int i = 0; i < modifiersCount; i++) {
                byte modKind = buffer.get();

                if (modKind == 7) {  // LocationOnly modifier
                    // Parse JDWP Location structure
                    io.debuggerx.protocol.jdwp.Location location = io.debuggerx.protocol.jdwp.Location.read(buffer, idSizes);

                    // Store raw location data - MCP will resolve className and lineNumber via JDWP queries
                    BreakpointInfo info = new BreakpointInfo(
                        requestId,
                        location.getTypeTag(),
                        location.getClassId().asLong(),
                        location.getMethodId().asLong(),
                        location.getIndex(),
                        source
                    );
                    session.getGlobalBreakpoints().put(requestId, info);
                    log.info("[GlobalBreakpoint] Registered breakpoint requestId={} classId={} methodId={} index={} from client={}",
                        requestId, location.getClassId().asLong(), location.getMethodId().asLong(),
                        location.getIndex(), source.getChannel());

                    // Asynchronously resolve breakpoint to get className, methodName, lineNumber
                    BreakpointResolver.resolveBreakpoint(info);
                    return;
                }
                // Skip other modifier types (not implemented yet)
                // This is a simplified version - full implementation would parse all modKind types
            }
        } catch (Exception e) {
            log.error("[GlobalBreakpoint] Failed to parse breakpoint details: {}", e.getMessage(), e);
        }
    }
}
