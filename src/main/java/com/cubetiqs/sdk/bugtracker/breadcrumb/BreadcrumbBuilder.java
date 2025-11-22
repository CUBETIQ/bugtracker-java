package com.cubetiqs.sdk.bugtracker.breadcrumb;

import io.sentry.SentryLevel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Builder for creating breadcrumbs with more control and better developer experience.
 */
public class BreadcrumbBuilder {
    private final String message;
    private SentryLevel level;
    private String category;
    private final Map<String, String> data;

    public BreadcrumbBuilder(String message) {
        this.message = Objects.requireNonNull(message, "message");
        this.level = SentryLevel.INFO;
        this.data = new HashMap<>();
    }

    public BreadcrumbBuilder level(SentryLevel level) {
        this.level = Objects.requireNonNull(level, "level");
        return this;
    }

    public BreadcrumbBuilder category(String category) {
        this.category = Objects.requireNonNull(category, "category");
        return this;
    }

    public BreadcrumbBuilder withData(String key, String value) {
        Objects.requireNonNull(key, "data key");
        Objects.requireNonNull(value, "data value");
        this.data.put(key, value);
        return this;
    }

    public BreadcrumbBuilder withData(Map<String, String> data) {
        if (data != null) {
            this.data.putAll(data);
        }
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SentryLevel getLevel() {
        return level;
    }

    public String getCategory() {
        return category;
    }

    public Map<String, String> getData() {
        return Collections.unmodifiableMap(data);
    }

    @Override
    public String toString() {
        return "BreadcrumbBuilder{" +
                "message='" + message + '\'' +
                ", level=" + level +
                ", category='" + category + '\'' +
                ", data=" + data +
                '}';
    }
}
