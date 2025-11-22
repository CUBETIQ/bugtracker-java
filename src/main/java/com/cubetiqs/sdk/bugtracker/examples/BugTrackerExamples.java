package com.cubetiqs.sdk.bugtracker.examples;

import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import com.cubetiqs.sdk.bugtracker.breadcrumb.BreadcrumbBuilder;
import com.cubetiqs.sdk.bugtracker.breadcrumb.BreadcrumbManager;
import com.cubetiqs.sdk.bugtracker.context.ContextManager;
import com.cubetiqs.sdk.bugtracker.hook.BugTrackerHook;
import com.cubetiqs.sdk.bugtracker.logger.BugTrackerLogHandler;
import com.cubetiqs.sdk.bugtracker.transaction.TransactionManager;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Comprehensive examples demonstrating all BugTracker SDK features.
 * These examples show best practices for using the BugTracker SDK.
 */
public class BugTrackerExamples {
    private static final Logger logger = Logger.getLogger(BugTrackerExamples.class.getName());

    /**
     * Example 1: Basic initialization and error tracking
     */
    public static void basicInitialization() {
        // Create and initialize the client
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnvironment("production")
                .setRelease("1.0.0")
                .setDebugEnabled(false)
                .build();

        tracker.initialize();

        // Capture a simple exception
        try {
            throw new IllegalArgumentException("Invalid input");
        } catch (Exception e) {
            tracker.captureException(e);
        }

        // Capture a message
        tracker.captureMessage("Application started successfully", SentryLevel.INFO);

        tracker.close();
    }

    /**
     * Example 2: Context management - user and tags
     */
    public static void contextManagement() {
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();

        tracker.initialize();

        // Get the context manager
        ContextManager context = tracker.context();

        // Set user information
        context.setUser("user@example.com", "user@example.com", "john_doe");

        // Add tags
        context.addTag("environment", "production")
                .addTag("service", "payment")
                .addTag("region", "us-east-1");

        // Add custom data
        context.addExtra("request_id", "req-12345")
                .addExtra("processing_time_ms", 1234)
                .addExtra("retry_count", 0);

        // Now any captured events will include this context
        tracker.captureMessage("Payment processed successfully", SentryLevel.INFO);

        tracker.close();
    }

    /**
     * Example 3: Breadcrumb tracking
     */
    public static void breadcrumbTracking() {
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();

        tracker.initialize();

        BreadcrumbManager breadcrumbs = tracker.breadcrumbs();

        // Track user actions
        breadcrumbs.userAction("User clicked checkout button");
        breadcrumbs.userAction("User entered payment details");

        // Track HTTP requests
        breadcrumbs.http("GET", "/api/products", 200);
        breadcrumbs.http("POST", "/api/orders", 201);
        breadcrumbs.http("GET", "/api/users/123", 404);

        // Track database queries
        breadcrumbs.database("SELECT * FROM orders WHERE user_id = ?");
        breadcrumbs.database("INSERT INTO audit_log VALUES (...)");

        // Track errors
        breadcrumbs.error("Payment gateway timeout", "TimeoutException");

        // Custom breadcrumb with builder
        breadcrumbs.addBreadcrumb(
                new BreadcrumbBuilder("Cache miss on user profile")
                        .category("cache")
                        .level(SentryLevel.WARNING)
                        .withData("cache_key", "user_profile_123")
                        .withData("ttl_seconds", "3600")
        );

        // Now if an error occurs, all breadcrumbs are attached
        try {
            throw new RuntimeException("Something went wrong!");
        } catch (RuntimeException e) {
            tracker.captureException(e);
        }

        tracker.close();
    }

    /**
     * Example 4: Performance monitoring with transactions
     */
    public static void performanceMonitoring() {
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setTracesSampleRate(1.0) // 100% sampling for this example
                .build();

        tracker.initialize();

        // Create a transaction for an HTTP request
        try (TransactionManager transaction = TransactionManager.start("user-registration", "http.request")) {
            // Simulate database operation
            io.sentry.ISpan dbSpan = transaction.startChild("db.operation", "INSERT INTO users");
            try {
                simulateDatabaseInsert();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            dbSpan.finish();

            // Simulate email sending
            io.sentry.ISpan emailSpan = transaction.startChild("email.send", "Send welcome email");
            try {
                simulateEmailSend();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            emailSpan.finish();

            // Simulate external API call
            io.sentry.ISpan apiSpan = transaction.startChild("http.client", "POST /api/notifications");
            try {
                simulateApiCall();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            apiSpan.finish();

            // Transaction automatically finishes when exiting try block
        }

        tracker.close();
    }

    /**
     * Example 5: Lifecycle hooks for event transformation
     */
    public static void lifecycleHooks() {
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();

        // Add hook to redact sensitive data
        tracker.hooks().addHook((event, hint) -> {
            // Remove credit card data
            if (event.getRequest() != null && event.getRequest().getHeaders() != null) {
                event.getRequest().getHeaders().put("Authorization", "[REDACTED]");
                event.getRequest().getHeaders().put("X-API-Key", "[REDACTED]");
            }
            return event;
        });

        // Add hook to add custom tags
        tracker.hooks().addHook((event, hint) -> {
            event.setTag("processed_by", "bugtracker_sdk");
            event.setTag("timestamp", String.valueOf(System.currentTimeMillis()));
            return event;
        });

        // Add hook to filter specific errors
        tracker.hooks().addHook((event, hint) -> {
            // Drop all DEBUG level events
            if (event.getLevel() == SentryLevel.DEBUG) {
                return null; // Drop the event
            }
            return event;
        });

        tracker.initialize();

        // Now all events go through the hooks
        tracker.captureMessage("This message will be processed by hooks", SentryLevel.ERROR);

        tracker.close();
    }

    /**
     * Example 6: Java Util Logging integration
     */
    public static void loggingIntegration() {
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();

        tracker.initialize();

        // Attach BugTracker to JUL
        BugTrackerLogHandler.attach(tracker);

        // Now all log messages flow to Sentry
        logger.severe("Critical error occurred");           // Sends as ERROR
        logger.warning("This is a warning");                // Sends as WARNING
        logger.info("Application started");                 // Sends as INFO
        logger.fine("Debug information");                   // Sends as DEBUG

        tracker.close();
    }

    /**
     * Example 7: Exception handling with custom scope
     */
    public static void exceptionHandlingWithScope() {
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();

        tracker.initialize();

        try {
            processPayment("123456", 99.99);
        } catch (PaymentException e) {
            // Capture exception with custom context
            tracker.captureException(e, scope -> {
                scope.setLevel(SentryLevel.ERROR);
                scope.setTag("payment_method", "credit_card");
                scope.setTag("currency", "USD");
                scope.setExtra("amount", "99.99");
                scope.setExtra("retry_count", "3");
            });
        }

        tracker.close();
    }

    /**
     * Example 8: Best practices - Complete application example
     */
    public static void completeApplicationExample() {
        // Initialize at application startup
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn(System.getenv("SENTRY_DSN"))
                .setEnvironment(System.getenv("ENVIRONMENT"))
                .setRelease(System.getProperty("app.version"))
                .setDebugEnabled(false)
                .addDefaultTag("app", "my-service")
                .addDefaultTag("team", "backend")
                .build();

        tracker.initialize();

        // Attach logging
        BugTrackerLogHandler.attach(tracker);

        // Add security hooks
        tracker.hooks().addHook(BugTrackerHook.transformEvent(event -> {
            // Redact authorization headers
            if (event.getRequest() != null && event.getRequest().getHeaders() != null) {
                for (String headerName : new String[]{"Authorization", "X-API-Key", "X-Auth-Token"}) {
                    event.getRequest().getHeaders().put(headerName, "[REDACTED]");
                }
            }
            return event;
        }));

        // Add shutdown hook to flush events before shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Flushing Sentry events before shutdown");
            tracker.flush(Duration.ofSeconds(5));
            tracker.close();
        }));

        // Set global user context
        tracker.context().setUser(
                System.getProperty("app.user.id"),
                System.getProperty("app.user.email"),
                System.getProperty("app.user.name")
        );

        // Add global tags
        tracker.context()
                .addTag("server", System.getProperty("java.rmi.server.hostname"))
                .addTag("jvm_version", System.getProperty("java.version"))
                .addTag("os", System.getProperty("os.name"));

        // Now use the SDK throughout your application
        processRequest(tracker);
    }

    // Helper methods for examples
    private static void simulateDatabaseInsert() throws InterruptedException {
        Thread.sleep(100);
    }

    private static void simulateEmailSend() throws InterruptedException {
        Thread.sleep(200);
    }

    private static void simulateApiCall() throws InterruptedException {
        Thread.sleep(150);
    }

    private static void processPayment(String cardToken, double amount) throws PaymentException {
        if (amount <= 0) {
            throw new PaymentException("Invalid amount: " + amount);
        }
        // Payment processing logic
    }

    private static void processRequest(BugTrackerClient tracker) {
        // Your application logic here
        ContextManager context = tracker.context();
        BreadcrumbManager breadcrumbs = tracker.breadcrumbs();

        breadcrumbs.addBreadcrumb("Request received");
        breadcrumbs.http("POST", "/api/process", 200);

        try {
            context.addTag("request_id", "req-123456");
            // Process request
            breadcrumbs.addBreadcrumb("Request completed successfully", SentryLevel.INFO);
        } catch (Exception e) {
            tracker.captureException(e);
        }
    }

    /**
     * Custom exception for payment errors
     */
    public static class PaymentException extends Exception {
        public PaymentException(String message) {
            super(message);
        }

        public PaymentException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Example 9: Advanced Sentry client access for custom operations
     */
    public static void advancedSentryClientAccess() {
        BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();

        tracker.initialize();

        // Get direct access to the underlying Sentry client for advanced operations
        io.sentry.IHub sentryHub = tracker.getSentryClient();

        // Example: Push a new scope for isolation
        sentryHub.pushScope();
        try {
            sentryHub.configureScope(scope -> {
                scope.setTag("advanced_operation", "true");
                scope.setTag("operation_type", "custom_sentry_access");
                scope.setLevel(SentryLevel.WARNING);
                scope.setExtra("handler", "advanced_sentry_client_access");
            });
            tracker.captureMessage("Message with advanced scope configuration", SentryLevel.WARNING);
        } finally {
            sentryHub.popScope();
        }

        // Example: Create a manual transaction (advanced performance monitoring)
        io.sentry.ITransaction transaction = sentryHub.startTransaction("advanced-operation", "custom.operation");
        if (transaction != null) {
            // Create child spans
            io.sentry.ISpan span1 = transaction.startChild("custom.step.1", "First processing step");
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            span1.finish();

            io.sentry.ISpan span2 = transaction.startChild("custom.step.2", "Second processing step");
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            span2.finish();

            // Transaction automatically sends when finished
            transaction.finish();
        }

        tracker.close();
    }

    /**
     * Example 10: Enable/disable and error handling configuration
     */
    public static void enableDisableAndErrorHandling() {
        // Example 1: Completely disable BugTracker
        System.out.println("Example: Disabled BugTracker");
        BugTrackerClient disabledTracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnabled(false)  // Disable tracking
                .build();

        // All operations are no-ops when disabled
        disabledTracker.captureMessage("This won't be sent");
        disabledTracker.captureException(new RuntimeException("This error is not tracked"));
        disabledTracker.close();

        // Example 2: Graceful error handling with ignore errors enabled (default)
        System.out.println("\nExample: Resilient BugTracker (ignore errors)");
        BugTrackerClient resilientTracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnabled(true)
                .setIgnoreErrors(true)  // Don't crash if Sentry fails
                .setDebugEnabled(false)
                .build();

        // If Sentry fails, your application continues running
        resilientTracker.initialize();
        resilientTracker.captureMessage("Safe message - app won't crash if Sentry is down");
        resilientTracker.close();

        // Example 3: Strict mode - propagate errors
        System.out.println("\nExample: Strict BugTracker (propagate errors)");
        BugTrackerClient strictTracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnabled(true)
                .setIgnoreErrors(false)  // Propagate errors
                .setDebugEnabled(true)
                .build();

        try {
            strictTracker.initialize();
            strictTracker.captureMessage("Message in strict mode");
        } catch (Exception e) {
            logger.severe("Sentry initialization failed: " + e.getMessage());
        }
        strictTracker.close();

        // Example 4: Dynamic enable/disable based on configuration
        System.out.println("\nExample: Dynamic enable/disable");
        String bugTrackerEnabled = System.getenv("BUGTRACKER_ENABLED");
        boolean isEnabled = bugTrackerEnabled == null || !bugTrackerEnabled.equals("false");

        BugTrackerClient dynamicTracker = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnabled(isEnabled)  // Based on environment variable
                .setIgnoreErrors(true)
                .setEnvironment(System.getenv("ENVIRONMENT"))
                .build();

        dynamicTracker.initialize();
        if (dynamicTracker.getConfig().isEnabled()) {
            dynamicTracker.captureMessage("BugTracker is enabled");
        } else {
            System.out.println("BugTracker is disabled");
        }
        dynamicTracker.close();
    }

    public static void main(String[] args) {
        System.out.println("BugTracker SDK Examples");
        System.out.println("======================\n");

        // Note: These examples require a valid Sentry DSN to actually send events.
        // For testing, you can use a mock DSN or run against a local Sentry instance.

        System.out.println("1. Basic Initialization");
        System.out.println("2. Context Management");
        System.out.println("3. Breadcrumb Tracking");
        System.out.println("4. Performance Monitoring");
        System.out.println("5. Lifecycle Hooks");
        System.out.println("6. Logging Integration");
        System.out.println("7. Exception Handling with Scope");
        System.out.println("8. Complete Application Example");
        System.out.println("9. Advanced Sentry Client Access");
        System.out.println("10. Enable/Disable and Error Handling");

        System.out.println("\nTo run examples, uncomment the desired example in main()");
    }
}
