package com.cubetiqs.sdk.bugtracker.context;

import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import io.sentry.protocol.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages context information for events (users, tags, extras, and custom data).
 * Provides a fluent API for setting context information.
 */
public class ContextManager {
    private final BugTrackerClient client;
    private final Map<String, String> tags;
    private final Map<String, Object> extras;
    private User currentUser;

    public ContextManager(BugTrackerClient client) {
        this.client = Objects.requireNonNull(client, "client");
        this.tags = new HashMap<>();
        this.extras = new HashMap<>();
    }

    public ContextManager setUser(User user) {
        this.currentUser = Objects.requireNonNull(user, "user");
        client.setUser(user);
        return this;
    }

    public ContextManager setUser(String userId) {
        User user = new User();
        user.setId(userId);
        return setUser(user);
    }

    public ContextManager setUser(String userId, String email, String username) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername(username);
        return setUser(user);
    }

    public ContextManager addTag(String key, String value) {
        Objects.requireNonNull(key, "tag key");
        Objects.requireNonNull(value, "tag value");
        tags.put(key, value);
        client.setTag(key, value);
        return this;
    }

    public ContextManager addTags(Map<String, String> tags) {
        if (tags != null) {
            tags.forEach(this::addTag);
        }
        return this;
    }

    public ContextManager addExtra(String key, Object value) {
        Objects.requireNonNull(key, "extra key");
        Objects.requireNonNull(value, "extra value");
        extras.put(key, value);
        client.setExtra(key, value.toString());
        return this;
    }

    public ContextManager addExtras(Map<String, Object> extras) {
        if (extras != null) {
            extras.forEach(this::addExtra);
        }
        return this;
    }

    public ContextManager clearUser() {
        this.currentUser = null;
        client.setUser(null);
        return this;
    }

    public ContextManager clearContext() {
        tags.clear();
        extras.clear();
        currentUser = null;
        client.setUser(null);
        // Note: IScope doesn't have clear* methods, so we just reset our local state
        return this;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Map<String, String> getTags() {
        return new HashMap<>(tags);
    }

    public Map<String, Object> getExtras() {
        return new HashMap<>(extras);
    }
}
