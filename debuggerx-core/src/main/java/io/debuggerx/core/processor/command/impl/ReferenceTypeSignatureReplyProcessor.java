package io.debuggerx.core.processor.command.impl;

import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.packet.BreakpointInfo;
import io.debuggerx.protocol.packet.JdwpPacket;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Processes ReferenceType.Signature replies to resolve class names
 * @author ouwu
 */
@Slf4j
public class ReferenceTypeSignatureReplyProcessor implements CommandProcessor {

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
                log.warn("[ReferenceTypeSignature] Breakpoint not found for requestId={}", breakpointRequestId);
                session.getPendingResolutions().remove(packet.getHeader().getId());
                return Collections.emptyList();
            }

            // Parse the signature string
            int signatureLength = byteBuffer.getInt();
            byte[] signatureBytes = new byte[signatureLength];
            byteBuffer.get(signatureBytes);
            String signature = new String(signatureBytes, StandardCharsets.UTF_8);

            // Convert JVM signature format to readable class name
            // e.g., "Ljava/lang/String;" -> "java.lang.String"
            String className = parseClassName(signature);

            // Update the breakpoint with resolved class name (keep existing methodName and lineNumber)
            String currentMethodName = breakpoint.getMethodName();
            int currentLineNumber = breakpoint.getLineNumber();
            breakpoint.setResolvedInfo(className, currentMethodName, currentLineNumber);

            log.info("[ReferenceTypeSignature] Resolved breakpoint requestId={} className={}",
                breakpointRequestId, className);

            // Clean up
            session.getPendingResolutions().remove(packet.getHeader().getId());

        } catch (Exception e) {
            log.error("[ReferenceTypeSignature] Failed to process reply: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * Converts JVM signature format to human-readable class name.
     *
     * @param signature the JVM signature (e.g., "Ljava/lang/String;")
     * @return readable class name (e.g., "java.lang.String")
     */
    private String parseClassName(String signature) {
        if (signature.startsWith("L") && signature.endsWith(";")) {
            // Class type: remove L prefix and ; suffix, replace / with .
            return signature.substring(1, signature.length() - 1).replace('/', '.');
        }
        // For other types (arrays, primitives), return as-is for now
        return signature;
    }
}
