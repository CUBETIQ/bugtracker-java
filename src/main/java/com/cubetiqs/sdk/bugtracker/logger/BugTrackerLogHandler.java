package com.cubetiqs.sdk.bugtracker.logger;

import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import io.sentry.SentryLevel;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.Objects;

/**
 * Java Util Logging handler that sends logs to BugTracker/Sentry.
 * Automatically converts JUL log levels to Sentry levels.
 */
public class BugTrackerLogHandler extends Handler {
    private final BugTrackerClient client;
    private volatile boolean enabled;

    public BugTrackerLogHandler(BugTrackerClient client) {
        this.client = Objects.requireNonNull(client, "client");
        this.enabled = true;
    }

    @Override
    public void publish(LogRecord record) {
        if (!enabled || !isLoggable(record)) {
            return;
        }

        try {
            SentryLevel level = convertLevel(record.getLevel().intValue());
            String message = getFormatter() != null ? getFormatter().format(record) : record.getMessage();

            if (record.getThrown() != null) {
                client.captureException(record.getThrown(), scope -> {
                    scope.setLevel(level);
                    scope.setTag("logger", record.getLoggerName());
                    scope.setExtra("method", record.getSourceMethodName());
                    scope.setExtra("class", record.getSourceClassName());
                });
            } else {
                client.captureMessage(message, level);
            }
        } catch (Exception e) {
            // Silently ignore errors to avoid breaking logging
        }
    }

    @Override
    public void flush() {
        if (client.isInitialized()) {
            client.flush();
        }
    }

    @Override
    public void close() {
        flush();
        enabled = false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private SentryLevel convertLevel(int javaLevel) {
        if (javaLevel >= java.util.logging.Level.SEVERE.intValue()) {
            return SentryLevel.ERROR;
        } else if (javaLevel >= java.util.logging.Level.WARNING.intValue()) {
            return SentryLevel.WARNING;
        } else if (javaLevel >= java.util.logging.Level.INFO.intValue()) {
            return SentryLevel.INFO;
        } else if (javaLevel >= java.util.logging.Level.FINE.intValue()) {
            return SentryLevel.DEBUG;
        } else {
            return SentryLevel.DEBUG;
        }
    }

    public static BugTrackerLogHandler attach(BugTrackerClient client) {
        BugTrackerLogHandler handler = new BugTrackerLogHandler(client);
        Logger.getLogger("").addHandler(handler);
        return handler;
    }

    public static BugTrackerLogHandler attach(BugTrackerClient client, String loggerName) {
        BugTrackerLogHandler handler = new BugTrackerLogHandler(client);
        Logger.getLogger(loggerName).addHandler(handler);
        return handler;
    }
}
