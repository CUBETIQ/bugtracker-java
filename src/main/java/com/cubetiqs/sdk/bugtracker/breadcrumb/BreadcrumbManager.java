package com.cubetiqs.sdk.bugtracker.breadcrumb;

import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import io.sentry.SentryLevel;
import java.util.Map;
import java.util.Objects;

/**
 * Manages breadcrumb creation and categorization.
 * Provides convenient methods for different types of breadcrumbs.
 */
public class BreadcrumbManager {
    private final BugTrackerClient client;

    public BreadcrumbManager(BugTrackerClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    public void addBreadcrumb(String message) {
        addBreadcrumb(message, SentryLevel.INFO);
    }

    public void addBreadcrumb(String message, SentryLevel level) {
        client.addBreadcrumb(message, level, null);
    }

    public void addBreadcrumb(BreadcrumbBuilder builder) {
        Objects.requireNonNull(builder, "builder");
        var breadcrumb = new io.sentry.Breadcrumb();
        breadcrumb.setMessage(builder.getMessage());
        breadcrumb.setLevel(builder.getLevel());
        if (builder.getCategory() != null) {
            breadcrumb.setCategory(builder.getCategory());
        }
        if (!builder.getData().isEmpty()) {
            for (Map.Entry<String, String> entry : builder.getData().entrySet()) {
                breadcrumb.setData(entry.getKey(), entry.getValue());
            }
        }
        io.sentry.Sentry.addBreadcrumb(breadcrumb);
    }

    public void http(String method, String url, int statusCode) {
        var builder = new BreadcrumbBuilder(method + " " + url)
                .category("http")
                .withData("status_code", String.valueOf(statusCode));
        addBreadcrumb(builder);
    }

    public void database(String query) {
        var builder = new BreadcrumbBuilder("Database query")
                .category("database")
                .level(SentryLevel.DEBUG)
                .withData("query", query);
        addBreadcrumb(builder);
    }

    public void userAction(String action) {
        var builder = new BreadcrumbBuilder(action)
                .category("user-action");
        addBreadcrumb(builder);
    }

    public void error(String message, String errorType) {
        var builder = new BreadcrumbBuilder(message)
                .category("error")
                .level(SentryLevel.ERROR)
                .withData("error_type", errorType);
        addBreadcrumb(builder);
    }

    public void warning(String message) {
        addBreadcrumb(message, SentryLevel.WARNING);
    }

    public void debug(String message) {
        addBreadcrumb(message, SentryLevel.DEBUG);
    }
}
