package com.cubetiqs.sdk.bugtracker.hook;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages multiple hooks and chains their execution.
 */
public class HookManager {
    private final List<BugTrackerHook> hooks;

    public HookManager() {
        this.hooks = new ArrayList<>();
    }

    public HookManager addHook(BugTrackerHook hook) {
        Objects.requireNonNull(hook, "hook");
        hooks.add(hook);
        return this;
    }

    public SentryEvent executeBeforeSend(SentryEvent event, Hint hint) {
        SentryEvent current = event;
        for (BugTrackerHook hook : hooks) {
            if (current == null) {
                break;
            }
            current = hook.beforeSend(current, hint);
        }
        return current;
    }

    public int getHookCount() {
        return hooks.size();
    }

    public void clear() {
        hooks.clear();
    }
}
