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

## 使用场景

- 多人开发时竞争同一个remote debug端口，经常需要等待别人的释放。部署debuggerX可支持多人同时断点，无惧等待
- 服务器断点端口不对外开放，无法使用remote debug。将debuggerX部署在跳板机中与业务服务断点端口连接，客户端通过连接debuggerX可实现断点

## 使用方法

默认与提供服务的jvm部署在一起（JDWP端口默认为**5005** 代理端口默认为**55005**）

```shell
nohup java -jar debuggerx-bootstrap-1.0-SNAPSHOT.jar > ~/logs/debuggerX.log 2>&1 &
```

### 可自定义参数

```shell
# 提供调试功能jvm服务地址
-DjvmServerHost=localhost
# 提供调试功能jvm服务端口
-DjvmServerPort=5005
# 调试器代理端口
-DdebuggerProxyPort=55005
```


