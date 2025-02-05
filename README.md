# DebuggerX

# DebuggerX

DebuggerX 是一个 Java 调试代理工具，支持多个调试器同时连接到同一个被调试程序，实现多人协同远程调试。通过 JDWP 协议转发，它可以让多个开发者在不同位置同时对一个 Java 应用进行调试，共享断点、变量查看等调试状态。
DebuggerX is a Java debugging proxy tool that allows multiple debuggers to connect to the same debuggee simultaneously, enabling collaborative remote debugging. Through JDWP protocol forwarding, it enables multiple developers to debug a Java application from different locations at the same time, sharing debugging states such as breakpoints and variable inspection.
## 功能特性

- JDWP 协议支持
- 多会话管理
- 实时数据包转发
- 调试连接管理
- 自动握手处理

## 架构设计

项目分为以下几个核心模块：

- **debuggerx-common**: 通用工具和常量定义
- **debuggerx-protocol**: JDWP 协议实现
- **debuggerx-core**: 核心业务逻辑和会话管理
- **debuggerx-transport**: 网络传输层实现
- **debuggerx-bootstrap**: 启动和配置管理
