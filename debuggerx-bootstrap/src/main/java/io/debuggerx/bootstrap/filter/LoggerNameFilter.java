package io.debuggerx.bootstrap.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A custom Logback filter that filters events based on an exact logger name match.
 * <p>
 * This provides a simple, reliable alternative to Janino-based evaluators,
 * especially in environments with SecurityManagers or complex classloaders.
 * </p>
 * <p>
 * Configuration in logback.xml:
 * <pre>{@code
 * <filter class="io.debuggerx.bootstrap.filter.LoggerNameFilter">
 *     <loggerName>jdwp.packets</loggerName>
 *     <onMatch>ACCEPT</onMatch>
 *     <onMismatch>DENY</onMismatch>
 * </filter>
 * }</pre>
 * </p>
 */
public class LoggerNameFilter extends Filter<ILoggingEvent> {

    private String loggerName;
    private FilterReply onMatch = FilterReply.NEUTRAL;
    private FilterReply onMismatch = FilterReply.NEUTRAL;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        // RAW DEBUG: Use System.out.println for a definitive check if this method is invoked at all
        System.out.println("RAW DEBUG: LoggerNameFilter.decide() invoked for event from logger: " + event.getLoggerName() + " (configured for: " + loggerName + ")");

        if (!isStarted()) {
            addInfo("Filter for '" + loggerName + "' is not started, returning NEUTRAL.");
            return FilterReply.NEUTRAL;
        }

        String eventLoggerName = event.getLoggerName();
        boolean matches = eventLoggerName.equals(loggerName);

        // Debug: log the first 5 decisions to help diagnose
        if (debugCount < 5) {
            addInfo("Filter decision: eventLogger='" + eventLoggerName + "', configuredLogger='" + loggerName + "', matches=" + matches + ", returning=" + (matches ? onMatch : onMismatch));
            debugCount++;
        }

        if (matches) {
            return onMatch;
        } else {
            return onMismatch;
        }
    }

    private int debugCount = 0;

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public void setOnMatch(FilterReply onMatch) {
        this.onMatch = onMatch;
    }

    public void setOnMismatch(FilterReply onMismatch) {
        this.onMismatch = onMismatch;
    }

    @Override
    public void start() {
        if (this.loggerName != null && !this.loggerName.isEmpty()) {
            addInfo("Starting LoggerNameFilter: loggerName=" + loggerName + ", onMatch=" + onMatch + ", onMismatch=" + onMismatch);
            super.start();
            addInfo("LoggerNameFilter '" + loggerName + "' started status: " + isStarted()); // Added for explicit status check
        } else {
            addError("The <loggerName> property must be set for LoggerNameFilter.");
        }
    }
}
