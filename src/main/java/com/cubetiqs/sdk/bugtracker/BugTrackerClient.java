package com.cubetiqs.sdk.bugtracker;

import com.cubetiqs.sdk.bugtracker.breadcrumb.BreadcrumbManager;
import com.cubetiqs.sdk.bugtracker.context.ContextManager;
import com.cubetiqs.sdk.bugtracker.hook.HookManager;
import io.sentry.Breadcrumb;
import io.sentry.IScope;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.SentryOptions;
import io.sentry.protocol.User;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class BugTrackerClient implements AutoCloseable {
    private static final Duration DEFAULT_FLUSH_TIMEOUT = Duration.ofSeconds(2);

    private final BugTrackerConfig config;
    private final Object initializationLock = new Object();
    private final BreadcrumbManager breadcrumbManager;
    private final ContextManager contextManager;
    private final HookManager hookManager;

    private BugTrackerClient(BugTrackerConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.breadcrumbManager = new BreadcrumbManager(this);
        this.contextManager = new ContextManager(this);
        this.hookManager = new HookManager();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void initialize() {
        if (!config.isEnabled()) {
            return;  // Skip initialization if disabled
        }
        if (Sentry.isEnabled()) {
            return;
        }
        synchronized (initializationLock) {
            if (Sentry.isEnabled()) {
                return;
            }
            try {
                Sentry.init(this::configureOptions);
            } catch (Exception e) {
                if (config.isIgnoreErrors()) {
                    // Log but don't throw to prevent app crash
                    if (config.isDebugEnabled()) {
                        System.err.println("BugTracker initialization failed (ignoring): " + e.getMessage());
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    public boolean isInitialized() {
        return config.isEnabled() && Sentry.isEnabled();
    }

    public void captureException(Throwable exception) {
        if (!config.isEnabled()) {
            return;  // No-op when disabled
        }
        try {
            ensureInitialized();
            Sentry.captureException(exception);
        } catch (Exception e) {
            if (config.isIgnoreErrors() && config.isDebugEnabled()) {
                System.err.println("BugTracker captureException failed: " + e.getMessage());
            } else if (!config.isIgnoreErrors()) {
                throw e;
            }
        }
    }

    public void captureException(Throwable exception, Consumer<IScope> scopeConfigurator) {
        if (!config.isEnabled()) {
            return;  // No-op when disabled
        }
        try {
            ensureInitialized();
            if (scopeConfigurator == null) {
                captureException(exception);
                return;
            }
            Sentry.withScope(scope -> scopeConfigurator.accept(scope));
            Sentry.captureException(exception);
        } catch (Exception e) {
            if (config.isIgnoreErrors() && config.isDebugEnabled()) {
                System.err.println("BugTracker captureException failed: " + e.getMessage());
            } else if (!config.isIgnoreErrors()) {
                throw e;
            }
        }
    }

    public void captureMessage(String message) {
        captureMessage(message, SentryLevel.INFO);
    }

    public void captureMessage(String message, SentryLevel level) {
        if (!config.isEnabled()) {
            return;  // No-op when disabled
        }
        try {
            ensureInitialized();
            Sentry.captureMessage(message, level);
        } catch (Exception e) {
            if (config.isIgnoreErrors() && config.isDebugEnabled()) {
                System.err.println("BugTracker captureMessage failed: " + e.getMessage());
            } else if (!config.isIgnoreErrors()) {
                throw e;
            }
        }
    }

    public void addBreadcrumb(String message) {
        addBreadcrumb(message, SentryLevel.INFO, null);
    }

    public void addBreadcrumb(String message, SentryLevel level, Map<String, String> data) {
        if (!config.isEnabled()) {
            return;  // No-op when disabled
        }
        try {
            ensureInitialized();
            Breadcrumb breadcrumb = new Breadcrumb();
            breadcrumb.setMessage(message);
            breadcrumb.setLevel(level);
            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    breadcrumb.setData(entry.getKey(), entry.getValue());
                }
            }
            Sentry.addBreadcrumb(breadcrumb);
        } catch (Exception e) {
            if (config.isIgnoreErrors() && config.isDebugEnabled()) {
                System.err.println("BugTracker addBreadcrumb failed: " + e.getMessage());
            } else if (!config.isIgnoreErrors()) {
                throw e;
            }
        }
    }

    public void configureScope(Consumer<IScope> scopeConfigurator) {
        if (!config.isEnabled()) {
            return;  // No-op when disabled
        }
        try {
            ensureInitialized();
            if (scopeConfigurator == null) {
                return;
            }
            Sentry.configureScope(scopeConfigurator::accept);
        } catch (Exception e) {
            if (config.isIgnoreErrors() && config.isDebugEnabled()) {
                System.err.println("BugTracker configureScope failed: " + e.getMessage());
            } else if (!config.isIgnoreErrors()) {
                throw e;
            }
        }
    }

    public void setTag(String key, String value) {
        configureScope(scope -> scope.setTag(key, value));
    }

    public void setExtra(String key, String value) {
        configureScope(scope -> scope.setExtra(key, value));
    }

    public void setUser(User user) {
        configureScope(scope -> scope.setUser(user));
    }

    public void flush() {
        flush(DEFAULT_FLUSH_TIMEOUT);
    }

    public void flush(Duration timeout) {
        if (!config.isEnabled()) {
            return;  // No-op when disabled
        }
        try {
            ensureInitialized();
            if (timeout == null || timeout.isNegative()) {
                timeout = DEFAULT_FLUSH_TIMEOUT;
            }
            Sentry.flush(timeout.toMillis());
        } catch (Exception e) {
            if (config.isIgnoreErrors() && config.isDebugEnabled()) {
                System.err.println("BugTracker flush failed: " + e.getMessage());
            } else if (!config.isIgnoreErrors()) {
                throw e;
            }
        }
    }

    @Override
    public void close() {
        synchronized (initializationLock) {
            if (Sentry.isEnabled()) {
                Sentry.close();
            }
        }
    }

    public BugTrackerConfig getConfig() {
        return config;
    }

    /**
     * Gets the underlying Sentry client instance.
     * 
     * This allows direct access to Sentry SDK functionality for advanced use cases
     * where BugTracker wrapper methods are insufficient.
     * 
     * @return the Sentry IHub instance for direct Sentry API access
     * @throws IllegalStateException if called before initialization
     */
    public io.sentry.IHub getSentryClient() {
        ensureInitialized();
        return Sentry.getCurrentHub();
    }

    public BreadcrumbManager breadcrumbs() {
        return breadcrumbManager;
    }

    public ContextManager context() {
        return contextManager;
    }

    public HookManager hooks() {
        return hookManager;
    }

    private void ensureInitialized() {
        if (!Sentry.isEnabled()) {
            initialize();
        }
    }

    void configureOptions(SentryOptions options) {
        config.getDsn().ifPresent(options::setDsn);
        config.getEnvironment().ifPresent(options::setEnvironment);
        config.getRelease().ifPresent(options::setRelease);
        config.getServerName().ifPresent(options::setServerName);
        config.getSampleRate().ifPresent(options::setSampleRate);
        config.getTracesSampleRate().ifPresent(options::setTracesSampleRate);
        options.setDebug(config.isDebugEnabled());
        
        // Configure hook integration
        options.setBeforeSend((event, hint) -> {
            if (hookManager.getHookCount() > 0) {
                return hookManager.executeBeforeSend(event, hint);
            } else if (!config.getDefaultTags().isEmpty()) {
                config.getDefaultTags().forEach(event::setTag);
            }
            return event;
        });
    }

    public static final class Builder {
        private final BugTrackerConfig.Builder configBuilder = BugTrackerConfig.builder();

        public Builder setDsn(String dsn) {
            configBuilder.setDsn(dsn);
            return this;
        }

        public Builder setEnvironment(String environment) {
            configBuilder.setEnvironment(environment);
            return this;
        }

        public Builder setRelease(String release) {
            configBuilder.setRelease(release);
            return this;
        }

        public Builder setServerName(String serverName) {
            configBuilder.setServerName(serverName);
            return this;
        }

        public Builder setSampleRate(Double sampleRate) {
            configBuilder.setSampleRate(sampleRate);
            return this;
        }

        public Builder setTracesSampleRate(Double tracesSampleRate) {
            configBuilder.setTracesSampleRate(tracesSampleRate);
            return this;
        }

        public Builder setDebugEnabled(boolean debugEnabled) {
            configBuilder.setDebugEnabled(debugEnabled);
            return this;
        }

        /**
         * Enable or disable BugTracker error tracking.
         * When disabled, all capture methods become no-ops without throwing errors.
         * 
         * Default: true (enabled)
         * 
         * @param enabled true to enable error tracking, false to disable
         * @return this builder for chaining
         */
        public Builder setEnabled(boolean enabled) {
            configBuilder.setEnabled(enabled);
            return this;
        }

        /**
         * Enable or disable error suppression when Sentry is unavailable.
         * When enabled (true), initialization failures won't crash the application.
         * 
         * Default: true (ignore errors)
         * 
         * @param ignoreErrors true to ignore Sentry initialization errors, false to propagate them
         * @return this builder for chaining
         */
        public Builder setIgnoreErrors(boolean ignoreErrors) {
            configBuilder.setIgnoreErrors(ignoreErrors);
            return this;
        }

        public Builder addDefaultTag(String key, String value) {
            configBuilder.addDefaultTag(key, value);
            return this;
        }

        public BugTrackerClient build() {
            return new BugTrackerClient(configBuilder.build());
        }
    }
}
