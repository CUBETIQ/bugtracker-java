# BugTracker SDK - Getting Started Guide

## Table of Contents
1. [Installation](#installation)
2. [Basic Setup](#basic-setup)
3. [Common Use Cases](#common-use-cases)
4. [Configuration](#configuration)
5. [Troubleshooting](#troubleshooting)

## Installation

### Gradle

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.cubetiqs.sdk:bugtracker:1.0.0'
}
```

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cubetiqs.sdk</groupId>
    <artifactId>bugtracker</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Setup

### Minimal Configuration

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

public class Application {
    public static void main(String[] args) {
        // Create and initialize
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .build();
        
        tracker.initialize();
        
        // Your application code here
        
        // Clean up
        tracker.close();
    }
}
```

### Try-With-Resources (Recommended)

```java
try (BugTrackerClient tracker = BugTrackerClient.builder()
        .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
        .build()) {
    tracker.initialize();
    
    // Your application code here
}
```

### Environment-Based Configuration

```java
BugTrackerClient tracker = BugTrackerClient.builder()
    .setDsn(System.getenv("SENTRY_DSN"))
    .setEnvironment(System.getenv("APP_ENV"))
    .setRelease(System.getProperty("app.version"))
    .build();
```

## Common Use Cases

### 1. Capture Exceptions

```java
try {
    riskyOperation();
} catch (Exception e) {
    tracker.captureException(e);
}
```

### 2. Capture with Context

```java
try {
    processOrder(orderId);
} catch (Exception e) {
    tracker.captureException(e, scope -> {
        scope.setTag("order_id", orderId);
        scope.setExtra("customer", customerId);
    });
}
```

### 3. Send Messages

```java
// Info message
tracker.captureMessage("Application started", SentryLevel.INFO);

// Warning
tracker.captureMessage("High memory usage detected", SentryLevel.WARNING);

// Error
tracker.captureMessage("Failed to connect to database", SentryLevel.ERROR);
```

### 4. Track User Actions

```java
tracker.context().setUser(userId, userEmail, userName);

tracker.breadcrumbs().userAction("Clicked checkout button");
tracker.breadcrumbs().userAction("Entered payment details");
```

### 5. Track HTTP Requests

```java
// In your HTTP interceptor or filter
tracker.breadcrumbs().http("GET", "/api/users", 200);
tracker.breadcrumbs().http("POST", "/api/orders", 201);
tracker.breadcrumbs().http("GET", "/api/missing", 404);
```

### 6. Performance Monitoring

```java
try (TransactionManager transaction = TransactionManager.start("endpoint", "http.request")) {
    ISpan dbSpan = transaction.startChild("db.query");
    // Perform database operation
    dbSpan.finish();
    
    ISpan cacheSpan = transaction.startChild("cache.write");
    // Write to cache
    cacheSpan.finish();
}
```

### 7. Java Logging Integration

```java
import com.cubetiqs.sdk.bugtracker.logger.BugTrackerLogHandler;
import java.util.logging.Logger;

// Attach to logging
BugTrackerLogHandler.attach(tracker);

// Now logs flow to Sentry
Logger logger = Logger.getLogger("com.myapp");
logger.severe("Critical error");    // Sends to Sentry as ERROR
logger.warning("Warning message");  // Sends to Sentry as WARNING
logger.info("Info message");        // Sends to Sentry as INFO
```

## Configuration

### Full Configuration Example

```java
BugTrackerClient tracker = BugTrackerClient.builder()
    // Sentry configuration
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7-id")
    .setEnvironment("production")
    .setRelease("1.0.0")
    .setServerName("api-server-01")
    
    // Event filtering
    .setSampleRate(0.8)              // Sample 80% of events
    .setTracesSampleRate(0.5)        // Sample 50% of traces
    
    // Debug mode
    .setDebugEnabled(false)          // Set true to see SDK logs
    
    // Default tags (attached to every event)
    .addDefaultTag("app", "my-service")
    .addDefaultTag("team", "backend")
    .addDefaultTag("version", "1.0.0")
    
    .build();
```

### Configuration via Environment Variables

Set these environment variables instead of using the builder:

```bash
# Required
export BUGTRACKER_SENTRY_DSN="https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7-id"

# Optional
export ENVIRONMENT="production"
export APP_RELEASE="1.0.0"
```

Or use system properties:

```bash
java -Dbugtracker.sentry.dsn="https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7-id" \
     -Dapp.environment="production" \
     -jar your-app.jar
```

## Common Patterns

### Pattern 1: Global Error Handler

```java
Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
    tracker.captureException(throwable, scope -> {
        scope.setTag("thread", thread.getName());
        scope.setExtra("is_daemon", thread.isDaemon());
    });
});
```

### Pattern 2: Periodic Health Check

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    long memoryUsage = Runtime.getRuntime().totalMemory() / 1_000_000;
    tracker.context().addExtra("memory_mb", memoryUsage);
    
    if (memoryUsage > 1000) {
        tracker.captureMessage(
            "High memory usage: " + memoryUsage + "MB",
            SentryLevel.WARNING
        );
    }
}, 1, 5, TimeUnit.MINUTES);
```

### Pattern 3: Custom Data Redaction

```java
tracker.hooks().addHook((event, hint) -> {
    // Redact passwords
    if (event.getMessage() != null) {
        String message = event.getMessage().getFormatted();
        message = message.replaceAll("password=\\w+", "password=[REDACTED]");
        // Note: actual message replacement depends on Sentry API
    }
    
    // Redact headers
    if (event.getRequest() != null && event.getRequest().getHeaders() != null) {
        event.getRequest().getHeaders().put("Authorization", "[REDACTED]");
        event.getRequest().getHeaders().put("X-API-Key", "[REDACTED]");
    }
    
    return event;
});
```

### Pattern 4: Graceful Shutdown

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    System.out.println("Flushing events before shutdown...");
    tracker.flush(Duration.ofSeconds(5));
    tracker.close();
    System.out.println("Events flushed. Exiting.");
}));
```

## Troubleshooting

### Events not being sent?

1. **Check DSN**: Verify your DSN is correct
   ```java
   System.out.println(tracker.getConfig().getDsn());
   ```

2. **Enable debug mode**: See what's happening
   ```java
   BugTrackerClient tracker = BugTrackerClient.builder()
       .setDebugEnabled(true)  // Shows SDK logs
       .build();
   ```

3. **Check network**: Ensure you can reach Sentry
   ```bash
   curl -I https://sentry.io
   ```

4. **Verify initialization**: Make sure `initialize()` was called
   ```java
   if (!tracker.isInitialized()) {
       tracker.initialize();
   }
   ```

### Missing Context in Events?

Make sure context is set before capturing:

```java
// ✓ Correct
tracker.context().setUser(userId);
tracker.captureException(e);

// ✗ Wrong - context set after capture
tracker.captureException(e);
tracker.context().setUser(userId);
```

### High Memory Usage?

- Reduce sample rate: `setSampleRate(0.1)` (10% of events)
- Limit breadcrumbs via Sentry configuration
- Monitor transaction creation

### Events Being Filtered?

Check if hooks are dropping events:

```java
tracker.hooks().addHook((event, hint) -> {
    System.out.println("Processing event: " + event);
    return event;  // Don't return null unless intentionally dropping
});
```

## Next Steps

1. **Read the full [README.md](../README.md)** for comprehensive documentation
2. **Check [BugTrackerExamples.java](./examples/BugTrackerExamples.java)** for more examples
3. **Explore Sentry documentation**: https://docs.sentry.io/
4. **Set up your Sentry project**: https://sentry.io/

## Support

- 📧 Email: oss@cubetiqs.com
- 🐛 Issues: https://github.com/cubetiq/bugtracker-java/issues
- 💬 Discussions: https://github.com/cubetiq/bugtracker-java/discussions
