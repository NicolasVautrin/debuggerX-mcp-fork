package io.debuggerx.bootstrap.http;

import com.sun.net.httpserver.HttpServer;
import io.debuggerx.common.constants.JdwpConstants;
import io.debuggerx.core.session.DebugSession;
import io.debuggerx.core.session.SessionManager;
import io.debuggerx.protocol.packet.BreakpointInfo;
import io.debuggerx.protocol.packet.JdwpHeader;
import io.debuggerx.protocol.packet.JdwpPacket;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple HTTP server to expose debuggerX state
 * @author ouwu
 */
@Slf4j
public class BreakpointHttpServer {
    private static final AtomicInteger packetIdGenerator = new AtomicInteger(200000);
    private HttpServer server;
    private final int port;

    public BreakpointHttpServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // GET /breakpoints - List all global breakpoints
            server.createContext("/breakpoints", exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    DebugSession session = SessionManager.getInstance().findJvmServerSession();
                    if (session == null) {
                        sendResponse(exchange, 500, "{\"error\": \"No debug session active\"}");
                        return;
                    }

                    Map<Integer, BreakpointInfo> breakpoints = session.getGlobalBreakpoints();
                    StringBuilder json = new StringBuilder();
                    json.append("{\"breakpoints\": [");

                    boolean first = true;
                    for (BreakpointInfo bp : breakpoints.values()) {
                        if (!first) json.append(",");
                        first = false;

                        // Include both raw and resolved data
                        String className = bp.getClassName() != null ? "\"" + escapeJson(bp.getClassName()) + "\"" : "null";
                        String methodName = bp.getMethodName() != null ? "\"" + escapeJson(bp.getMethodName()) + "\"" : "null";

                        json.append(String.format(
                            "{\"requestId\": %d, \"typeTag\": %d, \"classId\": %d, \"methodId\": %d, \"codeIndex\": %d, \"createdAt\": %d, \"className\": %s, \"methodName\": %s, \"lineNumber\": %d}",
                            bp.getRequestId(),
                            bp.getTypeTag(),
                            bp.getClassId(),
                            bp.getMethodId(),
                            bp.getCodeIndex(),
                            bp.getCreatedAt(),
                            className,
                            methodName,
                            bp.getLineNumber()
                        ));
                    }

                    json.append("]}");
                    sendResponse(exchange, 200, json.toString());
                } else {
                    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
            });

            // DELETE /breakpoints/{requestId} - Clear a specific breakpoint
            server.createContext("/breakpoints/", exchange -> {
                if ("DELETE".equals(exchange.getRequestMethod())) {
                    try {
                        String path = exchange.getRequestURI().getPath();
                        String[] parts = path.split("/");

                        if (parts.length < 3 || parts[2].isEmpty()) {
                            sendResponse(exchange, 400, "{\"error\": \"Missing requestId in URL path\"}");
                            return;
                        }

                        int requestId = Integer.parseInt(parts[2]);

                        DebugSession session = SessionManager.getInstance().findJvmServerSession();
                        if (session == null) {
                            sendResponse(exchange, 500, "{\"error\": \"No debug session active\"}");
                            return;
                        }

                        // Check if breakpoint exists
                        if (!session.getGlobalBreakpoints().containsKey(requestId)) {
                            sendResponse(exchange, 404, String.format("{\"error\": \"Breakpoint with requestId %d not found\"}", requestId));
                            return;
                        }

                        // Send EventRequest.Clear command to JVM
                        boolean success = clearBreakpoint(session, requestId);

                        if (success) {
                            sendResponse(exchange, 200, String.format("{\"message\": \"Breakpoint %d cleared successfully\"}", requestId));
                        } else {
                            sendResponse(exchange, 500, String.format("{\"error\": \"Failed to clear breakpoint %d\"}", requestId));
                        }
                    } catch (NumberFormatException e) {
                        sendResponse(exchange, 400, "{\"error\": \"Invalid requestId format\"}");
                    } catch (Exception e) {
                        sendResponse(exchange, 500, String.format("{\"error\": \"%s\"}", escapeJson(e.getMessage())));
                    }
                } else {
                    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
            });

            server.setExecutor(null); // Use default executor
            server.start();
            log.info("[HTTP] Breakpoint API server started on port {}", port);
        } catch (IOException e) {
            log.error("[HTTP] Failed to start HTTP server: {}", e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("[HTTP] Breakpoint API server stopped");
        }
    }

    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Escape special characters for JSON strings
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Send EventRequest.Clear command to JVM to remove a breakpoint
     */
    private boolean clearBreakpoint(DebugSession session, int requestId) {
        try {
            Channel jvmChannel = session.getJvmServerChannel();
            if (jvmChannel == null || !jvmChannel.isActive()) {
                log.error("[HTTP] JVM channel not active, cannot clear breakpoint");
                return false;
            }

            int packetId = packetIdGenerator.incrementAndGet();

            // Build JDWP packet: EventRequest.Clear (CommandSet=15, Command=2)
            // Data: byte eventKind (2 = BREAKPOINT) + int requestId
            ByteBuffer buffer = ByteBuffer.allocate(5);
            buffer.put((byte) 2); // BREAKPOINT event kind
            buffer.putInt(requestId);

            JdwpHeader header = new JdwpHeader();
            header.setId(packetId);
            header.setFlags(JdwpConstants.FLAG_COMMAND);
            header.setCommandSet((byte) 15); // EventRequest
            header.setCommand((byte) 2); // Clear

            JdwpPacket packet = new JdwpPacket(header, buffer.array());

            // Register packet so response can be mapped
            session.getPacketMap().put(packetId, packet);

            jvmChannel.writeAndFlush(packet);
            log.info("[HTTP] Sent EventRequest.Clear for breakpoint requestId={}", requestId);

            // Remove from tracked breakpoints
            session.getGlobalBreakpoints().remove(requestId);

            return true;
        } catch (Exception e) {
            log.error("[HTTP] Failed to clear breakpoint {}: {}", requestId, e.getMessage(), e);
            return false;
        }
    }
}
