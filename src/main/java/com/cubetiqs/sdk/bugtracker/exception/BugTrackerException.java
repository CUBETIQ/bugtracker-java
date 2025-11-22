package com.cubetiqs.sdk.bugtracker.exception;

/**
 * Base exception for BugTracker SDK.
 * Wraps exceptions from the underlying Sentry integration.
 */
public class BugTrackerException extends RuntimeException {
    public BugTrackerException(String message) {
        super(message);
    }

    public BugTrackerException(String message, Throwable cause) {
        super(message, cause);
    }

    public BugTrackerException(Throwable cause) {
        super(cause);
    }
}
