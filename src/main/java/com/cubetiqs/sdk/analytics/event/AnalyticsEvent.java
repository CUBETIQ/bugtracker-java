package com.cubetiqs.sdk.analytics.event;

/**
 * Wrapper for analytics events sent to Umami.
 * Contains the payload and metadata for tracking.
 */
public class AnalyticsEvent {
    private final EventPayload payload;
    private final String type;
    private final long timestamp;
    private int retryCount;

    public AnalyticsEvent(EventPayload payload) {
        this(payload, "event");
    }

    public AnalyticsEvent(EventPayload payload, String type) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        this.payload = payload;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.retryCount = 0;
    }

    public EventPayload getPayload() {
        return payload;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    @Override
    public String toString() {
        return "AnalyticsEvent{" +
                "name='" + payload.getName() + '\'' +
                ", website='" + payload.getWebsite() + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", retryCount=" + retryCount +
                '}';
    }
}
