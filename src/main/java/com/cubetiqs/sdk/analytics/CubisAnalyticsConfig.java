package com.cubetiqs.sdk.analytics;

import java.time.Duration;

/**
 * Configuration for CubisAnalyticsClient.
 * Provides settings for Umami analytics integration.
 */
public class CubisAnalyticsConfig {
    private final String url;
    private final String websiteId;
    private final boolean enabled;
    private final int maxQueueSize;
    private final int maxRetries;
    private final Duration initialRetryDelay;
    private final Duration maxRetryDelay;
    private final Duration requestTimeout;
    private final int workerThreads;
    private final boolean debugEnabled;

    private CubisAnalyticsConfig(Builder builder) {
        this.url = builder.url;
        this.websiteId = builder.websiteId;
        this.enabled = builder.enabled;
        this.maxQueueSize = builder.maxQueueSize;
        this.maxRetries = builder.maxRetries;
        this.initialRetryDelay = builder.initialRetryDelay;
        this.maxRetryDelay = builder.maxRetryDelay;
        this.requestTimeout = builder.requestTimeout;
        this.workerThreads = builder.workerThreads;
        this.debugEnabled = builder.debugEnabled;
    }

    public String getUrl() {
        return url;
    }

    public String getWebsiteId() {
        return websiteId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getInitialRetryDelay() {
        return initialRetryDelay;
    }

    public Duration getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static Builder builder(String url, String websiteId) {
        return new Builder(url, websiteId);
    }

    public static class Builder {
        private final String url;
        private final String websiteId;
        private boolean enabled = true;
        private int maxQueueSize = 10000;
        private int maxRetries = 3;
        private Duration initialRetryDelay = Duration.ofMillis(500);
        private Duration maxRetryDelay = Duration.ofSeconds(30);
        private Duration requestTimeout = Duration.ofSeconds(10);
        private int workerThreads = 1;
        private boolean debugEnabled = false;

        private Builder(String url, String websiteId) {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("URL is required");
            }
            if (websiteId == null || websiteId.trim().isEmpty()) {
                throw new IllegalArgumentException("Website ID is required");
            }
            this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            this.websiteId = websiteId;
        }

        /**
         * Enable or disable event tracking.
         * When disabled, events are not queued or sent.
         * Default: true
         */
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Maximum number of events in the queue before dropping old events.
         * Default: 10000
         */
        public Builder setMaxQueueSize(int maxQueueSize) {
            if (maxQueueSize <= 0) {
                throw new IllegalArgumentException("Max queue size must be positive");
            }
            this.maxQueueSize = maxQueueSize;
            return this;
        }

        /**
         * Maximum number of retry attempts for failed requests.
         * Default: 3
         */
        public Builder setMaxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("Max retries cannot be negative");
            }
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Initial delay before first retry.
         * Default: 500ms
         */
        public Builder setInitialRetryDelay(Duration initialRetryDelay) {
            if (initialRetryDelay == null || initialRetryDelay.isNegative()) {
                throw new IllegalArgumentException("Initial retry delay must be positive");
            }
            this.initialRetryDelay = initialRetryDelay;
            return this;
        }

        /**
         * Maximum delay between retries (for exponential backoff).
         * Default: 30s
         */
        public Builder setMaxRetryDelay(Duration maxRetryDelay) {
            if (maxRetryDelay == null || maxRetryDelay.isNegative()) {
                throw new IllegalArgumentException("Max retry delay must be positive");
            }
            this.maxRetryDelay = maxRetryDelay;
            return this;
        }

        /**
         * HTTP request timeout.
         * Default: 10s
         */
        public Builder setRequestTimeout(Duration requestTimeout) {
            if (requestTimeout == null || requestTimeout.isNegative()) {
                throw new IllegalArgumentException("Request timeout must be positive");
            }
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Number of worker threads for processing events.
         * Default: 1
         */
        public Builder setWorkerThreads(int workerThreads) {
            if (workerThreads <= 0) {
                throw new IllegalArgumentException("Worker threads must be positive");
            }
            this.workerThreads = workerThreads;
            return this;
        }

        /**
         * Enable debug logging.
         * Default: false
         */
        public Builder setDebugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            return this;
        }

        public CubisAnalyticsConfig build() {
            return new CubisAnalyticsConfig(this);
        }
    }
}
