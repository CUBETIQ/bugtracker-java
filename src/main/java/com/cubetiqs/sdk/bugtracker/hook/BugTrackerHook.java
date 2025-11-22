package com.cubetiqs.sdk.bugtracker.hook;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import java.util.function.Function;

/**
 * Interface for lifecycle hooks that intercept events before and after they are sent.
 */
@FunctionalInterface
public interface BugTrackerHook {
    /**
     * Intercepts event before being sent to Sentry.
     * Can modify or drop events (return null to drop).
     *
     * @param event the event
     * @param hint additional event hint
     * @return the modified event, or null to drop the event
     */
    SentryEvent beforeSend(SentryEvent event, Hint hint);

    /**
     * Convenience method to create a hook that only transforms events.
     */
    static BugTrackerHook transformEvent(Function<SentryEvent, SentryEvent> transformer) {
        return (event, hint) -> transformer.apply(event);
    }
}
