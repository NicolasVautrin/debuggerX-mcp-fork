package io.debuggerx.core.processor.command.impl;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.enums.EventKind;
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
    }
}
