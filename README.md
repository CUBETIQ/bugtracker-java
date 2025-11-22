# BugTracker SDK for Java

A flexible, high-performance wrapper around Sentry for Java applications. BugTracker provides an easy-to-use API for error tracking, monitoring, and debugging across your Java platform applications.

## Features

- 🚀 **Simple API** - Intuitive builder pattern and fluent interfaces
- 📊 **Performance Monitoring** - Track transactions and distributed traces
- 🎯 **Flexible Context** - Manage user data, tags, and custom data easily
- 📝 **Breadcrumb Management** - Categorized breadcrumbs for better debugging
- 🔌 **Lifecycle Hooks** - Intercept and transform events before sending
- 📋 **Java Logging Integration** - Automatic JUL handler for seamless integration
- 🔒 **Thread-Safe** - Built with concurrent applications in mind
- ⚡ **Low Overhead** - Minimal performance impact on your application
- ☕ **Java 1.8 to Latest** - Supports Java 1.8 through Java 21+

## Installation

### Requirements

- **Java Version**: 1.8 or higher (tested on Java 8, 11, 17, and 21)
- **Build Tool**: Gradle 8.0+ or Maven 3.6+

### Add to Project

Add to your `build.gradle`:

```gradle
dependencies {
    implementation("com.cubetiqs.sdk:bugtracker:1.0.0")
}
```

Or with Maven `pom.xml`:

```xml
<dependency>
    <groupId>com.cubetiqs.sdk</groupId>
    <artifactId>bugtracker</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### Basic Initialization

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

// Create and initialize client
BugTrackerClient bugTracker = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setEnvironment("production")
    .setRelease("1.0.0")
    .setDebugEnabled(false)
    .build();

bugTracker.initialize();
```

### Capturing Exceptions

```java
// Simple exception capture
try {
    someRiskyOperation();
} catch (Exception e) {
    bugTracker.captureException(e);
}

// With custom context
try {
    processPayment(order);
} catch (Exception e) {
    bugTracker.captureException(e, scope -> {
        scope.setTag("order_id", order.getId());
        scope.setTag("user_id", user.getId());
        scope.setLevel(SentryLevel.ERROR);
    });
}
```

### Capturing Messages

```java
// Simple message
bugTracker.captureMessage("Application started");

// With level
bugTracker.captureMessage("Warning: High memory usage detected", SentryLevel.WARNING);
```

## Advanced Usage

### Context Management

Manage user information, tags, and extras globally or per-event:

```java
// Set current user
bugTracker.context()
    .setUser("user@example.com", "email@example.com", "username");

// Add tags
bugTracker.context()
    .addTag("environment", "staging")
    .addTag("service", "payment")
    .addTag("region", "us-east-1");

// Add custom data
bugTracker.context()
    .addExtra("request_id", "req-123456")
    .addExtra("processing_time_ms", 1234);

// Clear context
bugTracker.context().clearContext();
```

### Breadcrumb Tracking

Track user actions and events leading to errors:

```java
BreadcrumbManager breadcrumbs = bugTracker.breadcrumbs();

// Simple breadcrumb
breadcrumbs.addBreadcrumb("User logged in");

// HTTP request
breadcrumbs.http("GET", "/api/users/123", 200);

// Database query
breadcrumbs.database("SELECT * FROM users WHERE id = ?");

// User action
breadcrumbs.userAction("clicked checkout button");

// Error breadcrumb
breadcrumbs.error("Payment failed", "InvalidCardException");

// Custom breadcrumb with builder
breadcrumbs.addBreadcrumb(
    new BreadcrumbBuilder("Cache miss")
        .category("cache")
        .level(BreadcrumbLevel.WARNING)
        .withData("cache_key", "user_profile_123")
        .withData("ttl", "3600")
);
```

### Performance Monitoring

Track performance metrics and distributed traces:

```java
// Simple transaction
try (TransactionManager transaction = TransactionManager.start("user-registration", "http.request")) {
    // Start child spans for different operations
    ISpan dbSpan = transaction.startChild("db.query", "INSERT INTO users");
    // ... perform database operation
    dbSpan.finish();
    
    ISpan emailSpan = transaction.startChild("email.send", "Send welcome email");
    // ... send email
    emailSpan.finish();
}
// Transaction automatically finished at end of try block
```

### Lifecycle Hooks

Intercept and transform events before they're sent to Sentry:

```java
// Add hook to filter or transform events
bugTracker.hooks().addHook((event, hint) -> {
    // Drop events with specific tags
    if ("debug".equals(event.getTag("level"))) {
        return null; // Drop the event
    }
    
    // Transform the event
    event.setTag("processed_by", "bugtracker");
    return event;
});

// Add multiple hooks - they execute in order
bugTracker.hooks()
    .addHook(BugTrackerHook.transformEvent(event -> {
        event.setTag("first_hook", "true");
        return event;
    }))
    .addHook((event, hint) -> {
        event.setTag("second_hook", "true");
        return event;
    });
```

### Java Logging Integration

Automatically capture java.util.logging events:

```java
import com.cubetiqs.sdk.bugtracker.logger.BugTrackerLogHandler;

// Attach to root logger
BugTrackerLogHandler.attach(bugTracker);

// Or attach to specific logger
BugTrackerLogHandler.attach(bugTracker, "com.mycompany.myapp");

// Now all log messages flow to Sentry
Logger logger = Logger.getLogger("com.mycompany.myapp");
logger.severe("Critical error occurred");  // Sends to Sentry as ERROR
logger.warning("This is a warning");       // Sends to Sentry as WARNING
```

### Event Builder for Complex Events

```java
import com.cubetiqs.sdk.bugtracker.event.EventBuilder;

EventBuilder event = new EventBuilder("Payment processing failed", SentryLevel.ERROR)
    .addTag("payment_method", "credit_card")
    .addTag("currency", "USD")
    .addExtra("amount", 9999)
    .addExtra("retry_count", 3)
    .withException(new PaymentException("Card declined"));

// Send through the client
bugTracker.captureMessage(event.getMessage(), event.getLevel());
```

## Configuration

### Via Builder

```java
BugTrackerClient client = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setEnvironment("production")
    .setRelease("1.0.0-rc1")
    .setServerName("api-server-01")
    .setSampleRate(0.8)           // Sample 80% of events
    .setTracesSampleRate(0.5)     // Sample 50% of performance events
    .setDebugEnabled(false)        // Disable debug logging
    .addDefaultTag("app", "my-service")
    .addDefaultTag("team", "backend")
    .build();
```

### Via Environment Variables

BugTracker looks for configuration in this order:

1. **bugtracker.sentry.dsn** system property
2. **BUGTRACKER_SENTRY_DSN** environment variable
3. **SENTRY_DSN** environment variable (legacy)
4. Default DSN: `https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7`

```bash
export BUGTRACKER_SENTRY_DSN="https://your-dsn@sentry.io/project"
java -jar your-app.jar
```

## Best Practices

### 1. Use Try-with-Resources for Cleanup

```java
try (BugTrackerClient tracker = BugTrackerClient.builder()
        .setDsn("https://...")
        .build()) {
    tracker.initialize();
    // Use tracker
} // Automatically closes and flushes
```

### 2. Set User Context Early

```java
// After authentication
bugTracker.context()
    .setUser(userId, userEmail, username);
```

### 3. Add Meaningful Tags

```java
bugTracker.context()
    .addTag("service", "payment")
    .addTag("version", "1.0.0")
    .addTag("region", System.getenv("AWS_REGION"));
```

### 4. Flush Before Shutdown

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    bugTracker.flush(Duration.ofSeconds(5));
    bugTracker.close();
}));
```

### 5. Use Hooks to Filter Sensitive Data

```java
bugTracker.hooks().addHook((event, hint) -> {
    // Remove credit card data
    if (event.getRequest() != null) {
        event.getRequest().setHeader("Authorization", "[REDACTED]");
    }
    return event;
});
```

## Thread Safety

BugTracker is designed to be thread-safe:

- Context managers use thread-local storage internally (via Sentry)
- Multiple threads can safely call capture methods concurrently
- Breadcrumbs are thread-isolated

## Performance Considerations

- **Event Sampling**: Use `setSampleRate()` to reduce event volume
- **Traces Sampling**: Use `setTracesSampleRate()` for performance metrics
- **Async Processing**: Sentry handles event transmission asynchronously
- **Flushing**: Call `flush()` before shutdown to ensure events are sent

## Troubleshooting

### Events not being captured?

1. Check if client is initialized: `bugTracker.isInitialized()`
2. Verify DSN is correct
3. Enable debug mode: `.setDebugEnabled(true)`
4. Check network connectivity to Sentry server

### High memory usage?

1. Reduce sample rate
2. Limit breadcrumb count (via Sentry options)
3. Monitor transaction creation

### Missing context in events?

1. Verify context is set before capturing: `bugTracker.context().setUser(...)`
2. Check if hooks are dropping events
3. Ensure default tags are configured

## Examples

### Web Application (Spring Boot)

```java
@Configuration
public class SentryConfig {
    
    @Bean
    public BugTrackerClient bugTrackerClient() {
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn(System.getenv("SENTRY_DSN"))
            .setEnvironment(System.getenv("ENVIRONMENT"))
            .setRelease(appVersion)
            .build();
        tracker.initialize();
        return tracker;
    }
    
    @Bean
    public FilterRegistrationBean<SentryFilter> sentryFilter(BugTrackerClient tracker) {
        FilterRegistrationBean<SentryFilter> registration = 
            new FilterRegistrationBean<>(new SentryFilter(tracker));
        registration.addUrlPatterns("/*");
        return registration;
    }
}
```

### CLI Application

```java
public class MyApp {
    public static void main(String[] args) {
        try (BugTrackerClient tracker = BugTrackerClient.builder()
                .setDsn(System.getenv("SENTRY_DSN"))
                .setEnvironment("production")
                .build()) {
            tracker.initialize();
            BugTrackerLogHandler.attach(tracker);
            
            // Your application code
            runApplication();
        }
    }
}
```

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

MIT License - see LICENSE file for details

## Support

For issues, questions, or suggestions:
- 📧 Email: oss@cubetiqs.com
- 🐛 Issues: https://github.com/cubetiq/bugtracker-java/issues
- 💬 Discussions: https://github.com/cubetiq/bugtracker-java/discussions
