package io.debuggerx.core.processor.command.impl;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.packet.BreakpointInfo;
import io.debuggerx.protocol.packet.JdwpPacket;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Processes Method.LineTable replies to resolve line numbers
 * @author ouwu
 */
@Slf4j
public class MethodLineTableReplyProcessor implements CommandProcessor {

    @Override
    public List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet) {
        try {
            DebugSession session = SessionManager.getInstance().findJvmServerSession();
            Integer breakpointRequestId = session.getPendingResolutions().get(packet.getHeader().getId());

            if (breakpointRequestId == null) {
                // Not a resolution query, ignore
                return Collections.emptyList();
            }

            BreakpointInfo breakpoint = session.getGlobalBreakpoints().get(breakpointRequestId);
            if (breakpoint == null) {
                log.warn("[MethodLineTable] Breakpoint not found for requestId={}", breakpointRequestId);
                session.getPendingResolutions().remove(packet.getHeader().getId());
                return Collections.emptyList();
            }

            // Parse Method.LineTable reply
            // Format: start (long), end (long), lines (int), [lineCodeIndex (long), lineNumber (int)] * lines
            long start = byteBuffer.getLong();
            long end = byteBuffer.getLong();
            int lineCount = byteBuffer.getInt();

            // Find the line number for our codeIndex
            int resolvedLineNumber = -1;
            long targetCodeIndex = breakpoint.getCodeIndex();

            for (int i = 0; i < lineCount; i++) {
                long lineCodeIndex = byteBuffer.getLong();
                int lineNumber = byteBuffer.getInt();

                // Find the best match - the line entry at or before our code index
                if (lineCodeIndex <= targetCodeIndex) {
                    resolvedLineNumber = lineNumber;
                } else {
                    // We've passed our target code index
                    break;
                }
            }

            if (resolvedLineNumber == -1 && lineCount > 0) {
                // Fallback: use the first line if we couldn't find a better match
                byteBuffer.position(byteBuffer.position() - (lineCount * 12)); // Reset to read again
                byteBuffer.getLong(); // Skip first lineCodeIndex
                resolvedLineNumber = byteBuffer.getInt(); // Get first lineNumber
            }

            // Update the breakpoint with resolved line number (keep existing className and methodName)
            String currentClassName = breakpoint.getClassName();
            String currentMethodName = breakpoint.getMethodName();
            breakpoint.setResolvedInfo(currentClassName, currentMethodName, resolvedLineNumber);

            log.info("[MethodLineTable] Resolved breakpoint requestId={} lineNumber={} (codeIndex={}, lineCount={})",
                breakpointRequestId, resolvedLineNumber, targetCodeIndex, lineCount);

            // Clean up
            session.getPendingResolutions().remove(packet.getHeader().getId());

        } catch (Exception e) {
            log.error("[MethodLineTable] Failed to process reply: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }
}
