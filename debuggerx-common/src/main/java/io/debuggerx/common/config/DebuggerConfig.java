package io.debuggerx.common.config;

import lombok.Builder;
import lombok.Data;

/**
 * debug代理配置
 *
 * @author ouwu
 */
@Data
@Builder
public class DebuggerConfig {
    /**
     * 提供调试功能jvm服务端口
     */
    private int jvmServerPort;
    /**
     * 调试器代理端口
     */
    private int debuggerProxyPort;
    
    public static DebuggerConfig getDefault() {
        return DebuggerConfig.builder()
                .jvmServerPort(5005)
                .debuggerProxyPort(55005)
                .build();
    }

    @Override
    public String toString() {
        return "DebuggerConfig{" + "jvmServerPort=" + jvmServerPort +
                ", debuggerProxyPort=" + debuggerProxyPort +
                '}';
    }
}