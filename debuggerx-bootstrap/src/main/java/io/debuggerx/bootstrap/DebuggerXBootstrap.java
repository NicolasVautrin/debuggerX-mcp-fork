package io.debuggerx.bootstrap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import io.debuggerx.bootstrap.config.BootstrapConfig;
import io.debuggerx.common.config.DebuggerConfig;
import io.debuggerx.transport.server.DebugProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * 代理启动入口
 *
 * @author ouwu
 */
@Slf4j
public class DebuggerXBootstrap {

    /**
     * 日志配置
     */
    private static void configureLogging() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        // 创建控制台输出
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("console");

        // 设置输出格式
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %logger{36} - %msg%n");
        encoder.start();

        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        // 配置根日志记录器
        Logger rootLogger = loggerContext.getLogger(ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);

        // 配置特定包的日志级别
        loggerContext.getLogger("io.debuggerx").setLevel(Level.DEBUG);
        loggerContext.getLogger("io.netty").setLevel(Level.INFO);
    }

    public static void main(String[] args) {
        // 配置日志
        configureLogging();

        log.info("[Bootstrap] Starting DebuggerX...");

        try {
            // 加载配置
            log.info("[Bootstrap] Loading configuration...");
            DebuggerConfig config = BootstrapConfig.load();
            log.info("[Bootstrap] Configuration loaded: " + config);

            // 创建并启动服务器
            log.info("[Bootstrap] Creating proxy server...");
            DebugProxyServer server = new DebugProxyServer(config);
            log.info("[Bootstrap] Starting proxy server...");
            server.start();
        } catch (InterruptedException e) {
            log.error("[Bootstrap] Failed to start DebuggerX: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}