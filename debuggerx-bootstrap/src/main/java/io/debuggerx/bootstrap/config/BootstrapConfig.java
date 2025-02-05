package io.debuggerx.bootstrap.config;

import io.debuggerx.common.config.DebuggerConfig;

/**
 * 启动配置类
 *
 * @author ouwu
 */
public class BootstrapConfig {

    private BootstrapConfig() {
    }


    public static DebuggerConfig load() {
        return DebuggerConfig.getDefault();
    }

} 