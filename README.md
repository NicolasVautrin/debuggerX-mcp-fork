# DebuggerX - MCP Fork

> **This is a fork of [debuggerX](https://github.com/ouoou/debuggerX) with critical improvements for AI-assisted debugging via [MCP (Model Context Protocol)](https://modelcontextprotocol.io) and [Claude Code](https://claude.com/claude-code) integration.**

[![Original Project](https://img.shields.io/badge/original-ouoou%2FdebuggerX-blue)](https://github.com/ouoou/debuggerX)
[![MCP Integration](https://img.shields.io/badge/MCP-integrated-green)](https://modelcontextprotocol.io)

## What is DebuggerX?

DebuggerX is a Java debugging proxy tool that allows **multiple debuggers to connect to the same JVM simultaneously**, enabling collaborative remote debugging. Through JDWP protocol forwarding, it enables multiple developers (or AI assistants!) to debug a Java application at the same time, sharing debugging states such as breakpoints and variable inspection.

## Why This Fork?

The original debuggerX is excellent for human developers using IDEs, but lacked critical features for **AI-assisted debugging** and **programmatic control**. This fork adds:

### 🔧 Critical Bug Fixes

- **Fixed NullPointerException** in breakpoint resolution system
- **Fixed packet routing** for internal BreakpointResolver queries
- All breakpoints now properly resolve to `className` + `lineNumber`

### ✨ New Features

1. **Breakpoint Resolution**
   - Resolves raw JDWP IDs to human-readable names
   - Shows `className` and `lineNumber` for all breakpoints
   - Essential for AI understanding of debug state

2. **HTTP API for Programmatic Control**
   - `GET /breakpoints` - List all breakpoints (with resolved info)
   - `DELETE /breakpoints/{requestId}` - Clear specific breakpoint
   - Port: `debuggerProxyPort + 1` (default: 55006)

3. **MCP Integration Ready**
   - 21 JDWP tools for [Claude Code](https://claude.com/claude-code)
   - AI can inspect variables, set breakpoints, step through code
   - Compatible with existing IDE debuggers (IntelliJ, Eclipse, etc.)

4. **Build Automation**
   - `build-and-copy.bat` script for easy rebuilds
   - Automatic JAR backup before building

📖 **For detailed technical explanation, see [FORK_RATIONALE.md](./FORK_RATIONALE.md)**

## Architecture

```
┌─────────────────────┐
│   Claude Code AI    │ ← NEW: AI-assisted debugging
└──────────┬──────────┘
           │ MCP Protocol
┌──────────▼──────────┐
│  MCP JDWP Server    │ ← NEW: 21 JDWP tools
└──────────┬──────────┘
           │ HTTP API (55006) + JDI (55005)
┌──────────▼──────────┐
│  debuggerX Proxy    │ ← IMPROVED: Breakpoint resolution
│  - HTTP endpoints   │   + DELETE endpoint
│  - Resolution       │   + NPE fixes
└──────────┬──────────┘
           │ JDWP (5005)
┌──────────▼──────────┐
│    Java App/JVM     │
└─────────────────────┘
```

## Core Modules

- **debuggerx-common**: Common utilities and constants
- **debuggerx-protocol**: JDWP protocol implementation
- **debuggerx-core**: Core business logic and session management
- **debuggerx-transport**: Network transport layer
- **debuggerx-bootstrap**: Startup and configuration management
- **debuggerx-bootstrap/http**: HTTP API server (NEW)

## Quick Start

### 1. Build the Proxy

```bash
# Using Maven (Windows)
mvn clean package

# Or use the automated script (backs up current JAR)
build-and-copy.bat
```

### 2. Start the Proxy

Deploy alongside your JVM (default JDWP port: **5005**, proxy port: **55005**, HTTP API: **55006**):

```bash
nohup java -jar debuggerx-bootstrap-1.0-SNAPSHOT.jar > ~/logs/debuggerX.log 2>&1 &
```

### 3. Connect Your Debugger

**IntelliJ IDEA / Eclipse:**
- Host: `localhost`
- Port: `55005` (instead of 5005)

**Claude Code (via MCP):**
- MCP server auto-connects to proxy
- Use tools like `jdwp_list_all_breakpoints()`, `jdwp_get_locals()`, etc.

### 4. Use HTTP API (Optional)

```bash
# List all breakpoints (with resolved className + lineNumber)
curl http://localhost:55006/breakpoints

# Clear specific breakpoint
curl -X DELETE http://localhost:55006/breakpoints/17
```

## Configuration

### Custom Parameters

```bash
# JVM server address (default: localhost)
-DjvmServerHost=localhost

# JVM JDWP port (default: 5005)
-DjvmServerPort=5005

# Debugger proxy port (default: 55005)
-DdebuggerProxyPort=55005

# HTTP API port is automatically: debuggerProxyPort + 1 (default: 55006)
```

## Use Cases

### 1. Multi-Developer Debugging
Multiple developers can connect to the same JVM simultaneously without competing for the debug port.

### 2. AI-Assisted Debugging (NEW)
Claude Code can analyze breakpoints, inspect variables, and step through code alongside human developers.

```
User: "Claude, why is this HTTP request failing?"

Claude:
1. jdwp_list_all_breakpoints() → Sees IntelliJ breakpoints
2. jdwp_get_threads() → Finds suspended HTTP thread
3. jdwp_get_stack(threadId) → Analyzes stack trace
4. jdwp_get_locals(threadId, 0) → Examines request variables
5. jdwp_invoke_method(threadId, requestId, "getBody") → Reads request body
6. jdwp_resume_thread(threadId) → Resumes after analysis
```

### 3. Secure Remote Debugging
Deploy debuggerX on a bastion host to access JVMs in restricted networks.

### 4. Test Automation (NEW)
Programmatically set breakpoints, trigger requests, verify state, and clean up via HTTP API.

## MCP Integration

To use with Claude Code, install the companion MCP server:

```bash
# Clone the MCP JDWP server
git clone https://github.com/YOUR_USERNAME/mcp-jdwp-java.git

# Build and configure in Claude Code settings
# See mcp-jdwp-java/README.md for details
```

Available MCP tools (21 total):
- Connection: `jdwp_connect`, `jdwp_disconnect`, `jdwp_get_version`
- Navigation: `jdwp_get_threads`, `jdwp_get_stack`, `jdwp_get_locals`, `jdwp_get_fields`
- Control: `jdwp_resume`, `jdwp_step_over`, `jdwp_step_into`, `jdwp_step_out`
- Breakpoints: `jdwp_set_breakpoint`, `jdwp_list_all_breakpoints`, `jdwp_clear_breakpoint_by_id`, `jdwp_clear_all_breakpoints`
- Events: `jdwp_get_events`, `jdwp_configure_exception_monitoring`
- And more...

## Comparison with Original

| Feature | Original debuggerX | This Fork |
|---------|-------------------|-----------|
| Multi-debugger support | ✅ Working | ✅ Working |
| Breakpoint resolution | ❌ Crashes (NPE) | ✅ Fixed |
| HTTP GET /breakpoints | ✅ Raw IDs only | ✅ Resolved info |
| HTTP DELETE /breakpoints | ❌ Missing | ✅ Added |
| Proxy logs | ❌ Lost | ✅ Captured |
| MCP integration | ❌ Impossible | ✅ 21 tools |
| JSON parsing | N/A | ✅ Jackson |
| AI support | ❌ No | ✅ Claude Code |

## Documentation

- [FORK_RATIONALE.md](./FORK_RATIONALE.md) - Detailed explanation of changes and why fork was necessary
- [Original Documentation](https://zread.ai/wuou-learn/debuggerX/1-overview) (Chinese)

## Contributing

This fork is primarily maintained for MCP/Claude Code integration. For general debuggerX improvements, consider contributing to the [original project](https://github.com/ouoou/debuggerX).

Contributions specific to AI-assisted debugging are welcome!

## License

Same as original debuggerX project.

## Credits

- **Original Author**: [@ouoou](https://github.com/ouoou) - [debuggerX](https://github.com/ouoou/debuggerX)
- **Fork Enhancements**: Nicolas Vautrin & Claude Code
- **MCP Integration**: Built with [Anthropic's MCP](https://modelcontextprotocol.io)

---

**🤖 This fork enables AI-powered debugging while maintaining 100% compatibility with traditional IDE debuggers.**
