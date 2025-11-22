package com.cubetiqs.sdk.bugtracker.event;

import io.sentry.IScope;
import io.sentry.SentryLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent builder for creating and configuring events before sending to Sentry.
 * Provides high-level abstractions for building events with a focus on developer experience.
 */
public class EventBuilder {
    private final String message;
    private final SentryLevel level;
    private final Map<String, String> tags;
    private final Map<String, Object> extras;
    private Consumer<IScope> scopeConfigurator;
    private Throwable exception;

    public EventBuilder(String message) {
        this(message, SentryLevel.INFO);
    }

    public EventBuilder(String message, SentryLevel level) {
        this.message = Objects.requireNonNull(message, "message");
        this.level = Objects.requireNonNull(level, "level");
        this.tags = new HashMap<>();
        this.extras = new HashMap<>();
    }

    public EventBuilder addTag(String key, String value) {
        Objects.requireNonNull(key, "tag key");
        Objects.requireNonNull(value, "tag value");
        this.tags.put(key, value);
        return this;
    }

    public EventBuilder addTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags.putAll(tags);
        }
        return this;
    }

    public EventBuilder addExtra(String key, Object value) {
        Objects.requireNonNull(key, "extra key");
        Objects.requireNonNull(value, "extra value");
        this.extras.put(key, value);
        return this;
    }

    public EventBuilder addExtras(Map<String, Object> extras) {
        if (extras != null) {
            this.extras.putAll(extras);
        }
        return this;
    }

    public EventBuilder withException(Throwable exception) {
        this.exception = Objects.requireNonNull(exception, "exception");
        return this;
    }

    public EventBuilder withScopeConfigurator(Consumer<IScope> scopeConfigurator) {
        this.scopeConfigurator = Objects.requireNonNull(scopeConfigurator, "scopeConfigurator");
        return this;
    }

    public EventBuilder withLevel(SentryLevel level) {
        return new EventBuilder(this.message, Objects.requireNonNull(level, "level"));
    }

    public String getMessage() {
        return message;
    }

    public SentryLevel getLevel() {
        return level;
    }

    public Map<String, String> getTags() {
        return new HashMap<>(tags);
    }

    public Map<String, Object> getExtras() {
        return new HashMap<>(extras);
    }

    public Throwable getException() {
        return exception;
    }

    public Consumer<IScope> getScopeConfigurator() {
        return scopeConfigurator;
    }

    @Override
    public String toString() {
        return "EventBuilder{" +
                "message='" + message + '\'' +
                ", level=" + level +
                ", tags=" + tags +
                ", extras=" + extras +
                ", exception=" + exception +
                '}';
    }
}
