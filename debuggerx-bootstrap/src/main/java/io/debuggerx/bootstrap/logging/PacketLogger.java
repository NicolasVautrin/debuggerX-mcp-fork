package io.debuggerx.bootstrap.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

public class PacketLogger {
    private static final String LOGGER_NAME = "jdwp.packets"; // The name used in JdwpPacketDecoder
    private static final String LOG_FILE_NAME = "debuggerx-packets.log";
    private static final String FILE_NAME_PATTERN = "debuggerx-packets.%d{yyyy-MM-dd}.log";
    private static final String LOG_PATTERN = "%d{HH:mm:ss.SSS} - %msg%n"; // No highlight for file
    private static final int MAX_HISTORY = 7;

    private static volatile Logger packetLoggerInstance;

    public static Logger getLogger() {
        if (packetLoggerInstance == null) {
            synchronized (PacketLogger.class) {
                if (packetLoggerInstance == null) {
                    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

                    // Create the logger instance
                    Logger logger = loggerContext.getLogger(LOGGER_NAME);
                    logger.setLevel(Level.OFF); // DISABLED: Packet logging suspended to reduce disk usage
                    logger.setAdditive(false); // Crucial: prevent propagation to root

                    // Create the encoder for the file appender
                    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
                    encoder.setPattern(LOG_PATTERN);
                    encoder.setContext(loggerContext);
                    encoder.start();

                    // Create the rolling file appender
                    RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
                    rollingFileAppender.setFile(LOG_FILE_NAME);
                    rollingFileAppender.setEncoder(encoder);
                    rollingFileAppender.setContext(loggerContext);
                    rollingFileAppender.setImmediateFlush(true); // Ensure immediate write

                    // Create the rolling policy
                    TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
                    rollingPolicy.setFileNamePattern(FILE_NAME_PATTERN);
                    rollingPolicy.setMaxHistory(MAX_HISTORY);
                    rollingPolicy.setParent(rollingFileAppender); // Link policy to appender
                    rollingPolicy.setContext(loggerContext);
                    rollingPolicy.start();

                    rollingFileAppender.setRollingPolicy(rollingPolicy);
                    rollingFileAppender.start();

                    // Add the appender to the logger
                    logger.addAppender(rollingFileAppender);
                    packetLoggerInstance = logger;
                }
            }
        }
        return packetLoggerInstance;
    }
}
