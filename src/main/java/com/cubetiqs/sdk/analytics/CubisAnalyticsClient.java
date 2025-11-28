package com.cubetiqs.sdk.analytics;

import com.cubetiqs.sdk.analytics.event.AnalyticsEvent;
import com.cubetiqs.sdk.analytics.event.EventPayload;
import com.cubetiqs.sdk.analytics.internal.EventQueue;
import com.cubetiqs.sdk.analytics.internal.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main client for Cubis Analytics powered by Umami.
 * Provides high-performance, non-blocking event tracking with automatic retry and error handling.
 *
 * <p>Features:
 * <ul>
 *   <li>Non-blocking event tracking</li>
 *   <li>Automatic retry with exponential backoff</li>
 *   <li>Bounded queue to prevent memory issues</li>
 *   <li>Thread-safe operations</li>
 *   <li>Graceful degradation on errors</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * CubisAnalyticsClient client = CubisAnalyticsClient.builder()
 *     .setUrl("https://analytics.ctdn.dev")
 *     .setWebsiteId("your-website-id")
 *     .setEnabled(true)
 *     .build();
 *
 * client.initialize();
 *
 * // Track events
 * client.track("page-view", "/home", "Home Page");
 * client.track("button-click", "/checkout", "Checkout Button",
 *     Map.of("amount", 99.99, "currency", "USD"));
 *
 * // Cleanup
 * client.close();
 * }</pre>
 */
public class CubisAnalyticsClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(CubisAnalyticsClient.class);

    private final CubisAnalyticsConfig config;
    private final AtomicBoolean initialized;
    private final String sessionId;  // Persistent session ID for this client instance
    private volatile String currentUserId;  // Current identified user ID (can change on login/logout)
    private EventQueue eventQueue;
    private EventSender eventSender;

    private CubisAnalyticsClient(CubisAnalyticsConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.initialized = new AtomicBoolean(false);
        this.sessionId = java.util.UUID.randomUUID().toString();  // Generate once per client instance
    }

    /**
     * Create a new builder for CubisAnalyticsClient.
     *
     * @param url Umami server URL
     * @param websiteId Website ID in Umami
     * @return Builder instance
     */
    public static Builder builder(String url, String websiteId) {
        return new Builder(url, websiteId);
    }

    /**
     * Initialize the analytics client.
     * Must be called before tracking events.
     *
     * @return true if initialized successfully
     */
    public synchronized boolean initialize() {
        if (initialized.get()) {
            logger.info("Analytics client already initialized");
            return true;
        }

        if (!config.isEnabled()) {
            logger.info("Analytics is disabled, skipping initialization");
            initialized.set(true);
            return true;
        }

        try {
            logger.info("Initializing Cubis Analytics Client...");
            logger.info("URL: {}", config.getUrl());
            logger.info("Website ID: {}", config.getWebsiteId());

            this.eventSender = new EventSender(config);
            this.eventQueue = new EventQueue(config, eventSender);

            initialized.set(true);
            logger.info("Cubis Analytics Client initialized successfully");
            return true;

        } catch (Exception e) {
            logger.error("Failed to initialize analytics client: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if the client is initialized.
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Check if analytics is enabled.
     */
    public boolean isEnabled() {
        return config.isEnabled();
    }

    /**
     * Track an event with name, url, and title.
     *
     * @param name Event name
     * @param url Page URL
     * @param title Page title
     * @return true if event was queued successfully
     */
    public boolean track(String name, String url, String title) {
        return track(name, url, title, null);
    }

    /**
     * Track an event with name, url, title, and custom data.
     *
     * @param name Event name
     * @param url Page URL
     * @param title Page title
     * @param data Custom event data
     * @return true if event was queued successfully
     */
    public boolean track(String name, String url, String title, Map<String, Object> data) {
        if (!initialized.get()) {
            logger.warn("Analytics client not initialized, call initialize() first");
            return false;
        }

        if (!config.isEnabled()) {
            logger.debug("Analytics disabled, skipping event: {}", name);
            return false;
        }

        try {
            // Use currentUserId if identified, otherwise use sessionId
            String eventId = currentUserId != null ? currentUserId : sessionId;

            EventPayload.Builder payloadBuilder = EventPayload.builder(config.getWebsiteId(), name)
                    .setUrl(url)
                    .setTitle(title)
                    .setId(eventId);

            if (data != null && !data.isEmpty()) {
                payloadBuilder.setData(data);
            }

            EventPayload payload = payloadBuilder.build();
            AnalyticsEvent event = new AnalyticsEvent(payload);

            return eventQueue.enqueue(event);

        } catch (Exception e) {
            logger.error("Error tracking event: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Track an event with a custom payload builder.
     *
     * @param payloadBuilder Custom payload builder
     * @return true if event was queued successfully
     */
    public boolean track(EventPayload.Builder payloadBuilder) {
        if (!initialized.get()) {
            logger.warn("Analytics client not initialized, call initialize() first");
            return false;
        }

        if (!config.isEnabled()) {
            logger.debug("Analytics disabled, skipping event");
            return false;
        }

        try {
            EventPayload payload = payloadBuilder.build();
            AnalyticsEvent event = new AnalyticsEvent(payload);
            return eventQueue.enqueue(event);

        } catch (Exception e) {
            logger.error("Error tracking event: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Track a simple event with just a name.
     *
     * @param name Event name
     * @return true if event was queued successfully
     */
    public boolean track(String name) {
        return track(name, "/", name);
    }

    /**
     * Track a pageview event (will show in Umami Overview/Statistics).
     * Pageviews do not have event names and will count as regular page visits.
     *
     * @param url Page URL
     * @param title Page title
     * @return true if event was queued successfully
     */
    public boolean trackPageView(String url, String title) {
        if (!initialized.get()) {
            logger.warn("Analytics client not initialized, call initialize() first");
            return false;
        }

        if (!config.isEnabled()) {
            logger.debug("Analytics disabled, skipping pageview: {}", url);
            return false;
        }

        try {
            // Use currentUserId if identified, otherwise use sessionId
            String eventId = currentUserId != null ? currentUserId : sessionId;

            EventPayload.Builder payloadBuilder = EventPayload.pageviewBuilder(config.getWebsiteId())
                    .setUrl(url)
                    .setTitle(title)
                    .setId(eventId);

            EventPayload payload = payloadBuilder.build();
            AnalyticsEvent event = new AnalyticsEvent(payload);

            return eventQueue.enqueue(event);

        } catch (Exception e) {
            logger.error("Error tracking pageview: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Identify a user for analytics tracking.
     * Sends an "identify" event with user information.
     *
     * @param userId User identifier
     * @return true if event was queued successfully
     */
    public boolean identify(String userId) {
        return identify(userId, null);
    }

    /**
     * Identify a user with additional user data.
     * Sets the current user ID which will be used for all subsequent events.
     * Can be called multiple times to switch users (e.g., login/logout scenarios).
     *
     * @param userId User identifier
     * @param userData Additional user data (name, email, properties, etc.)
     * @return true if event was queued successfully
     */
    public boolean identify(String userId, Map<String, Object> userData) {
        if (!initialized.get()) {
            logger.warn("Analytics client not initialized, call initialize() first");
            return false;
        }

        if (!config.isEnabled()) {
            logger.debug("Analytics disabled, skipping identify: {}", userId);
            return false;
        }

        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("User ID is required for identify");
            return false;
        }

        try {
            // Clear cache when user identity changes to ensure fresh tracking
            if (eventSender != null) {
                eventSender.clearCache();
            }

            // Update current user ID - all subsequent events will use this
            this.currentUserId = userId;
            logger.info("User identified: {} (all subsequent events will use this user ID)", userId);

            // Build payload with userId as the id field
            EventPayload.Builder payloadBuilder = EventPayload.builder(config.getWebsiteId(), "identify")
                    .setId(userId)
                    .setUrl("/")
                    .setTitle("User Identified");

            // Add user data if provided
            if (userData != null && !userData.isEmpty()) {
                payloadBuilder.setData(userData);
            }

            EventPayload payload = payloadBuilder.build();
            AnalyticsEvent event = new AnalyticsEvent(payload, "identify");

            return eventQueue.enqueue(event);

        } catch (Exception e) {
            logger.error("Error identifying user: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Clear the current user identification.
     * Subsequent events will use the session ID instead of user ID.
     * Useful for logout scenarios.
     */
    public void clearIdentity() {
        logger.info("Clearing user identity. Subsequent events will use session ID.");

        // Clear cache when identity is cleared to ensure fresh tracking
        if (eventSender != null) {
            eventSender.clearCache();
        }

        this.currentUserId = null;
    }

    /**
     * Get the current user ID if identified, null otherwise.
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Get current queue size.
     */
    public int getQueueSize() {
        if (eventQueue == null) {
            return 0;
        }
        return eventQueue.getQueueSize();
    }

    /**
     * Get total events queued since initialization.
     */
    public long getEventsQueued() {
        if (eventQueue == null) {
            return 0;
        }
        return eventQueue.getEventsQueued();
    }

    /**
     * Get total events processed since initialization.
     */
    public long getEventsProcessed() {
        if (eventQueue == null) {
            return 0;
        }
        return eventQueue.getEventsProcessed();
    }

    /**
     * Get total events dropped since initialization.
     */
    public long getEventsDropped() {
        if (eventQueue == null) {
            return 0;
        }
        return eventQueue.getEventsDropped();
    }

    /**
     * Get the configuration.
     */
    public CubisAnalyticsConfig getConfig() {
        return config;
    }

    /**
     * Flush pending events and wait for completion.
     *
     * @param timeout Maximum time to wait
     * @param unit Time unit
     * @return true if flush completed within timeout
     */
    public boolean flush(long timeout, TimeUnit unit) {
        if (eventQueue == null) {
            return true;
        }

        logger.info("Flushing {} pending events...", eventQueue.getQueueSize());

        long startTime = System.currentTimeMillis();
        long timeoutMs = unit.toMillis(timeout);

        while (eventQueue.getQueueSize() > 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMs) {
                logger.warn("Flush timeout, {} events remaining", eventQueue.getQueueSize());
                return false;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        logger.info("Flush complete");
        return true;
    }

    /**
     * Shutdown the client and clean up resources.
     */
    public synchronized void shutdown() {
        if (!initialized.get()) {
            return;
        }

        logger.info("Shutting down analytics client...");

        if (eventQueue != null) {
            eventQueue.shutdown(10, TimeUnit.SECONDS);
        }

        if (eventSender != null) {
            eventSender.shutdown();
        }

        initialized.set(false);
        logger.info("Analytics client shutdown complete");
    }

    @Override
    public void close() {
        shutdown();
    }

    /**
     * Builder for CubisAnalyticsClient.
     */
    public static class Builder {
        private final CubisAnalyticsConfig.Builder configBuilder;

        private Builder(String url, String websiteId) {
            this.configBuilder = CubisAnalyticsConfig.builder(url, websiteId);
        }

        public Builder setEnabled(boolean enabled) {
            configBuilder.setEnabled(enabled);
            return this;
        }

        public Builder setMaxQueueSize(int maxQueueSize) {
            configBuilder.setMaxQueueSize(maxQueueSize);
            return this;
        }

        public Builder setMaxRetries(int maxRetries) {
            configBuilder.setMaxRetries(maxRetries);
            return this;
        }

        public Builder setInitialRetryDelay(java.time.Duration initialRetryDelay) {
            configBuilder.setInitialRetryDelay(initialRetryDelay);
            return this;
        }

        public Builder setMaxRetryDelay(java.time.Duration maxRetryDelay) {
            configBuilder.setMaxRetryDelay(maxRetryDelay);
            return this;
        }

        public Builder setRequestTimeout(java.time.Duration requestTimeout) {
            configBuilder.setRequestTimeout(requestTimeout);
            return this;
        }

        public Builder setWorkerThreads(int workerThreads) {
            configBuilder.setWorkerThreads(workerThreads);
            return this;
        }

        public Builder setDebugEnabled(boolean debugEnabled) {
            configBuilder.setDebugEnabled(debugEnabled);
            return this;
        }

        public CubisAnalyticsClient build() {
            return new CubisAnalyticsClient(configBuilder.build());
        }
    }
}
