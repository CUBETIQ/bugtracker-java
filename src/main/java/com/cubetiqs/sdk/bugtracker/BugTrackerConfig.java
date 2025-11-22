package com.cubetiqs.sdk.bugtracker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class BugTrackerConfig {
    private static final String SYSTEM_PROPERTY_DSN = "bugtracker.sentry.dsn";
    private static final String ENV_VAR_DSN = "BUGTRACKER_SENTRY_DSN";
    private static final String LEGACY_ENV_VAR_DSN = "SENTRY_DSN";
    private static final String DEFAULT_DSN = "https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7";

    private static Supplier<String> systemPropertySupplier = () -> System.getProperty(SYSTEM_PROPERTY_DSN);
    private static Supplier<String> envSupplier = () -> System.getenv(ENV_VAR_DSN);
    private static Supplier<String> legacyEnvSupplier = () -> System.getenv(LEGACY_ENV_VAR_DSN);

    private final Optional<String> dsn;
    private final Optional<String> environment;
    private final Optional<String> release;
    private final Optional<String> serverName;
    private final Optional<Double> sampleRate;
    private final Optional<Double> tracesSampleRate;
    private final boolean debugEnabled;
    private final boolean enabled;
    private final boolean ignoreErrors;
    private final Map<String, String> defaultTags;

    private BugTrackerConfig(Builder builder) {
        this.dsn = Optional.ofNullable(resolveDsn(builder.dsn));
        this.environment = Optional.ofNullable(builder.environment);
        this.release = Optional.ofNullable(builder.release);
        this.serverName = Optional.ofNullable(builder.serverName);
        this.sampleRate = Optional.ofNullable(builder.sampleRate);
        this.tracesSampleRate = Optional.ofNullable(builder.tracesSampleRate);
        this.debugEnabled = builder.debugEnabled;
        this.enabled = builder.enabled;
        this.ignoreErrors = builder.ignoreErrors;
        this.defaultTags = Collections.unmodifiableMap(new LinkedHashMap<>(builder.defaultTags));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> getDsn() {
        return dsn;
    }

    public Optional<String> getEnvironment() {
        return environment;
    }

    public Optional<String> getRelease() {
        return release;
    }

    public Optional<String> getServerName() {
        return serverName;
    }

    public Optional<Double> getSampleRate() {
        return sampleRate;
    }

    public Optional<Double> getTracesSampleRate() {
        return tracesSampleRate;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    public Map<String, String> getDefaultTags() {
        return defaultTags;
    }

    static String getDefaultDsn() {
        return DEFAULT_DSN;
    }

    static void setSystemPropertySupplier(Supplier<String> supplier) {
        systemPropertySupplier = supplier;
    }

    static void setEnvSupplier(Supplier<String> supplier) {
        envSupplier = supplier;
    }

    static void setLegacyEnvSupplier(Supplier<String> supplier) {
        legacyEnvSupplier = supplier;
    }

    static void resetSuppliers() {
        systemPropertySupplier = () -> System.getProperty(SYSTEM_PROPERTY_DSN);
        envSupplier = () -> System.getenv(ENV_VAR_DSN);
        legacyEnvSupplier = () -> System.getenv(LEGACY_ENV_VAR_DSN);
    }

    private static String resolveDsn(String configuredDsn) {
        return firstNonEmpty(
                configuredDsn,
                systemPropertySupplier.get(),
                envSupplier.get(),
                legacyEnvSupplier.get(),
                DEFAULT_DSN
        );
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null) {
                String candidate = value.trim();
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public static final class Builder {
        private String dsn;
        private String environment;
        private String release;
        private String serverName;
        private Double sampleRate;
        private Double tracesSampleRate;
        private boolean debugEnabled;
        private boolean enabled = true;  // Enabled by default
        private boolean ignoreErrors = true;  // Ignore errors by default to avoid crashes
        private final Map<String, String> defaultTags = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder setDsn(String dsn) {
            this.dsn = dsn;
            return this;
        }

        public Builder setEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder setRelease(String release) {
            this.release = release;
            return this;
        }

        public Builder setServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder setSampleRate(Double sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public Builder setTracesSampleRate(Double tracesSampleRate) {
            this.tracesSampleRate = tracesSampleRate;
            return this;
        }

        public Builder setDebugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
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
            this.enabled = enabled;
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
            this.ignoreErrors = ignoreErrors;
            return this;
        }

        public Builder addDefaultTag(String key, String value) {
            Objects.requireNonNull(key, "default tag key");
            Objects.requireNonNull(value, "default tag value");
            this.defaultTags.put(key, value);
            return this;
        }

        public BugTrackerConfig build() {
            return new BugTrackerConfig(this);
        }
    }
}
