package com.cubetiqs.sdk.analytics.internal;

import com.cubetiqs.sdk.analytics.CubisAnalyticsConfig;
import com.cubetiqs.sdk.analytics.event.AnalyticsEvent;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP sender for analytics events with retry logic and exponential backoff.
 * Handles communication with Umami analytics server using Unirest.
 */
public class EventSender {
    private static final Logger logger = LoggerFactory.getLogger(EventSender.class);
    // Dynamic User-Agent based on system properties
    private static final String USER_AGENT = UserAgentBuilder.build();
    private static final String CONTENT_TYPE = "application/json";
    private static final String API_ENDPOINT = "/api/send";
    private static final String CACHE_HEADER = "x-umami-cache";

    private final CubisAnalyticsConfig config;
    private volatile String cacheValue;

    public EventSender(CubisAnalyticsConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;

        // Configure Unirest
        Unirest.config()
                .socketTimeout((int) config.getRequestTimeout().toMillis())
                .connectTimeout((int) config.getRequestTimeout().toMillis())
                .followRedirects(true)
                .enableCookieManagement(false);
    }

    /**
     * Send an event to Umami analytics with retry logic.
     *
     * @param event The event to send
     * @return true if sent successfully, false otherwise
     */
    public boolean send(AnalyticsEvent event) {
        if (event == null) {
            logger.warn("Event is null, skipping");
            return false;
        }

        int attempt = 0;
        Exception lastException = null;

        while (attempt <= config.getMaxRetries()) {
            try {
                if (attempt > 0) {
                    long delay = calculateBackoff(attempt);
                    logger.debug("Retry attempt {} after {}ms delay for event: {}",
                            attempt, delay, event.getPayload().getName());
                    Thread.sleep(delay);
                }

                boolean success = sendRequest(event);
                if (success) {
                    if (attempt > 0) {
                        logger.info("Successfully sent event after {} retries: {}",
                                attempt, event.getPayload().getName());
                    }
                    return true;
                }

                attempt++;
                event.incrementRetryCount();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Thread interrupted while sending event: {}", e.getMessage());
                return false;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                event.incrementRetryCount();
                logger.warn("Failed to send event (attempt {}): {}", attempt, e.getMessage());
            }
        }

        logger.error("Failed to send event after {} retries: {} - {}",
                config.getMaxRetries(),
                event.getPayload().getName(),
                lastException != null ? lastException.getMessage() : "unknown error");
        return false;
    }

    /**
     * Send HTTP request to Umami API using Unirest.
     */
    private boolean sendRequest(AnalyticsEvent event) {
        String url = config.getUrl() + API_ENDPOINT;
        String json = JsonSerializer.toJson(event);

        logger.debug("Sending event to: {}", url);
        logger.debug("Payload: {}", json);

        try {
            // Build request with headers
            kong.unirest.HttpRequestWithBody requestBuilder = Unirest.post(url)
                    .header("Content-Type", CONTENT_TYPE)
                    .header("User-Agent", USER_AGENT);

            // Add cache header if available
            if (cacheValue != null && !cacheValue.isEmpty()) {
                requestBuilder = requestBuilder.header(CACHE_HEADER, cacheValue);
                logger.debug("Using cached value: {}", cacheValue);
            }

            // Execute request
            HttpResponse<String> response = requestBuilder.body(json).asString();

            int statusCode = response.getStatus();
            String responseBody = response.getBody();

            logger.debug("Response code: {}", statusCode);
            logger.debug("Response body: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                // Extract and store cache value from response
                extractCacheFromResponse(responseBody);
                return true;
            } else {
                logger.warn("Server returned error {}: {}", statusCode, responseBody);
                return false;
            }

        } catch (Exception e) {
            logger.error("Exception sending request: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Calculate exponential backoff delay.
     */
    private long calculateBackoff(int attempt) {
        long delay = config.getInitialRetryDelay().toMillis() * (1L << (attempt - 1));
        long maxDelay = config.getMaxRetryDelay().toMillis();
        return Math.min(delay, maxDelay);
    }

    /**
     * Extract cache value from JSON response.
     * Response format: {"cache":"value","sessionId":"...","visitId":"..."}
     */
    private void extractCacheFromResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return;
        }

        try {
            // Simple JSON parsing for cache field
            int cacheIndex = responseBody.indexOf("\"cache\"");
            if (cacheIndex != -1) {
                int colonIndex = responseBody.indexOf(":", cacheIndex);
                if (colonIndex != -1) {
                    int startQuote = responseBody.indexOf("\"", colonIndex);
                    if (startQuote != -1) {
                        int endQuote = responseBody.indexOf("\"", startQuote + 1);
                        if (endQuote != -1) {
                            String cache = responseBody.substring(startQuote + 1, endQuote);
                            if (!cache.equals("boop") && !cache.isEmpty()) {
                                this.cacheValue = cache;
                                logger.debug("Cached value extracted: {}", cache);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract cache from response: {}", e.getMessage());
        }
    }

    /**
     * Clear cached value.
     * Should be called when user identity changes to ensure fresh tracking.
     */
    public void clearCache() {
        this.cacheValue = null;
        logger.debug("Cache cleared");
    }

    /**
     * Shutdown Unirest client.
     */
    public void shutdown() {
        try {
            Unirest.shutDown();
            logger.debug("Unirest client shutdown complete");
        } catch (Exception e) {
            logger.warn("Error shutting down Unirest: {}", e.getMessage());
        }
    }
}
