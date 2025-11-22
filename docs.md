# BugTracker Java SDK Documentation

Complete documentation for integrating and using the BugTracker Java SDK in your applications.

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
    - [Gradle](#gradle)
    - [Maven](#maven)
    - [Manual JAR Installation](#manual-jar-installation)
3. [Quick Start](#quick-start)
4. [Configuration](#configuration)
5. [Core Features](#core-features)
6. [API Reference](#api-reference)
7. [Examples](#examples)
8. [Troubleshooting](#troubleshooting)
9. [FAQ](#faq)

---

## Overview

BugTracker is a flexible Sentry wrapper SDK for Java applications that provides:

-   **Error Tracking**: Automatic capture and reporting of exceptions
-   **Performance Monitoring**: Transaction tracking and performance analysis
-   **Breadcrumb Tracking**: Track user actions and application flow
-   **Context Management**: Add custom context to error reports
-   **Multi-Version Support**: Works with Java 1.8 through Java 21
-   **Enable/Disable Toggle**: Turn tracking on/off without code changes
-   **Error Resilience**: Application continues running even if Sentry is unavailable
-   **Sentry Client Access**: Direct access to Sentry API for advanced use cases

### Key Features

✅ Simple, intuitive API
✅ Zero configuration required (works out of the box)
✅ Support for Java 1.8 - 21
✅ Automatic Sentry dependency management
✅ Graceful error handling and degradation
✅ Comprehensive logging integration
✅ Transaction and span support
✅ Breadcrumb tracking

---

## Installation

### Gradle

#### Step 1: Add Repository

Add the following to your `build.gradle`:

```gradle
repositories {
    mavenCentral()
    // If using snapshot builds:
    // maven {
    //     url "https://oss.sonatype.org/content/repositories/snapshots"
    // }
}
```

#### Step 2: Add Dependency

```gradle
dependencies {
    implementation 'com.cubetiqs:bugtracker:1.0.1'
}
```

#### Full Example

```gradle
plugins {
    id 'java'
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // BugTracker SDK
    implementation 'com.cubetiqs:bugtracker:1.0.1'

    // Other dependencies...
}
```

#### Verify Installation

After adding the dependency, run:

```bash
./gradlew dependencies
```

You should see `com.cubetiqs:bugtracker:1.0.1` in the dependency tree.

---

### Maven

#### Step 1: Add Repository (if needed)

Most Maven configurations automatically use Maven Central. If needed, add to `pom.xml`:

```xml
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

#### Step 2: Add Dependency

Add the following to your `pom.xml`:

```xml
<dependencies>
    <!-- BugTracker SDK -->
    <dependency>
        <groupId>com.cubetiqs</groupId>
        <artifactId>bugtracker</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

#### Full Example

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>central</id>
            <url>https://repo.maven.apache.org/maven2</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- BugTracker SDK -->
        <dependency>
            <groupId>com.cubetiqs</groupId>
            <artifactId>bugtracker</artifactId>
            <version>1.0.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Verify Installation

After adding the dependency, run:

```bash
mvn dependency:tree
```

You should see `com.cubetiqs:bugtracker:1.0.1` in the dependency tree.

---

### Manual JAR Installation

If you prefer to manage dependencies manually, follow these steps:

#### Step 1: Download JAR Files

Download the following JAR files:

1. **BugTracker Shadow JAR** (recommended - includes all dependencies):

    - Download: `bugtracker-1.0.1-all.jar` (4.6 MB)
    - Contains: BugTracker + all dependencies (Sentry, SLF4J, Logback, etc.)

2. **BugTracker Standard JAR** (requires manual dependency management):

    - Download: `bugtracker-1.0.1.jar` (31 KB)
    - Requires: Separate dependency JARs

3. **Required Dependencies** (if using standard JAR):
    - `sentry-7.10.0.jar` (or compatible version)
    - `sentry-logback-7.10.0.jar`
    - `sentry-jul-7.10.0.jar`
    - `slf4j-api-1.7.36.jar`
    - `logback-core-1.2.13.jar`
    - `logback-classic-1.2.13.jar`
    - `guava-30.1.1-jre.jar`

#### Step 2: Add JARs to Classpath

**Option A: Using Gradle**

```gradle
dependencies {
    // Add local JAR (shadow JAR with all dependencies)
    implementation files('libs/bugtracker-1.0.1-all.jar')
}
```

**Option B: Using Maven**

```xml
<dependency>
    <groupId>com.cubetiqs</groupId>
    <artifactId>bugtracker</artifactId>
    <version>1.0.1</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/bugtracker-1.0.1-all.jar</systemPath>
</dependency>
```

**Option C: Command Line (Java)**

```bash
# Compile with JAR in classpath
javac -cp bugtracker-1.0.1-all.jar MyApp.java

# Run with JAR in classpath
java -cp bugtracker-1.0.1-all.jar:. MyApp
```

**Option D: IDE Setup**

1. **IntelliJ IDEA**:

    - File → Project Structure → Libraries
    - Click `+` and select `Java`
    - Navigate to the JAR file and select it
    - Click OK

2. **Eclipse**:

    - Right-click project → Build Path → Configure Build Path
    - Libraries tab → Add External JARs
    - Select the JAR file
    - Click OK

3. **NetBeans**:
    - Right-click project → Properties
    - Libraries section → Add JAR/Folder
    - Select the JAR file
    - Click OK

#### Step 3: Verify Installation

Create a simple test class:

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

public class BugTrackerTest {
    public static void main(String[] args) {
        BugTrackerClient client = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();
        client.initialize();
        System.out.println("BugTracker loaded successfully!");
    }
}
```

Compile and run:

```bash
javac -cp bugtracker-1.0.1-all.jar BugTrackerTest.java
java -cp bugtracker-1.0.1-all.jar:. BugTrackerTest
```

---

## Quick Start

### Basic Setup (5 minutes)

1. **Initialize BugTracker** using the builder:

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

public class MyApp {
    public static void main(String[] args) {
        // Initialize with DSN from environment variable or system property
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .build();

        tracker.initialize();

        // Your application code...
    }
}
```

**Note**: The constructor is private. Always use `BugTrackerClient.builder()` to create instances.

2. **Set DSN via Environment or System Property**:

```bash
# Using environment variable (recommended)
export SENTRY_DSN="https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7"
java MyApp

# Or using system property
java -Dsentry.dsn="https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7" MyApp
```

3. **Automatic User Detection**:

BugTracker automatically detects and sets the current system user when no user is explicitly set:

```java
BugTrackerClient tracker = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .build();

tracker.initialize();

// System user is automatically detected from:
// - USER environment variable (Unix/Linux/Mac)
// - USERNAME environment variable (Windows)
// - user.name system property

// You can still override it:
tracker.context().setUser("user_123", "user@example.com", "john_doe");
```

4. **Capture Exceptions**:

```java
try {
    // Your code
    int result = 10 / 0;
} catch (Exception e) {
    tracker.captureException(e);
}
```

4. **Capture Messages**:

```java
tracker.captureMessage("User logged in successfully");
```

---

## Configuration

### Default Configuration

BugTracker works with zero configuration:

```java
// Works with environment variable or system property
BugTrackerClient tracker = new BugTrackerClient();
```

### Custom Configuration

Use the builder pattern for advanced configuration:

```java
BugTrackerClient tracker = new BugTrackerClient.Builder()
    .setDSN("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setEnvironment("production")
    .setRelease("1.0.0")
    .setServerName("app-server-1")
    .setEnabled(true)
    .setIgnoreErrors(true)
    .build();
```

### Configuration Options

| Option         | Type    | Default               | Description                                  |
| -------------- | ------- | --------------------- | -------------------------------------------- |
| `DSN`          | String  | `$SENTRY_DSN` env var | Sentry project DSN                           |
| `environment`  | String  | `"production"`        | Environment name (production, staging, etc.) |
| `release`      | String  | Auto-detected         | Application release version                  |
| `serverName`   | String  | Auto-detected         | Server/hostname name                         |
| `enabled`      | Boolean | `true`                | Enable/disable error tracking                |
| `ignoreErrors` | Boolean | `true`                | Gracefully handle Sentry errors              |

### Enable/Disable Tracking

```java
// Disable error tracking
BugTrackerClient tracker = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setEnabled(false)
    .build();

tracker.initialize();

// All methods become no-ops when disabled
tracker.captureException(e); // Does nothing
tracker.captureMessage("msg"); // Does nothing
```

### Error Resilience

```java
// Ignore errors - app continues even if Sentry fails
BugTrackerClient tracker = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setIgnoreErrors(true) // default
    .build();

tracker.initialize();

// Strict mode - errors propagate
BugTrackerClient tracker2 = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setIgnoreErrors(false)
    .build();

tracker2.initialize();
```

### Setting DSN

```java
// Option 1: Constructor
BugTrackerClient tracker = new BugTrackerClient("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7");

// Option 2: Builder
BugTrackerClient tracker = new BugTrackerClient.Builder()
    .setDSN("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .build();

// Option 3: Environment variable (SENTRY_DSN)
// Option 4: System property (-Dsentry.dsn=...)
```

---

## Core Features

### 1. Exception Tracking

Automatically capture and report exceptions:

```java
try {
    // Your code
} catch (Exception e) {
    tracker.captureException(e);
}
```

### 2. Message Logging

Log messages directly to Sentry:

```java
tracker.captureMessage("Application started");
tracker.captureMessage("User action completed");
tracker.captureMessage("Database query executed");
```

### 3. Breadcrumbs

Track user actions and application flow:

```java
tracker.addBreadcrumb("user_login", "User logged in", "info");
tracker.addBreadcrumb("api_call", "Called /api/users", "debug");
tracker.addBreadcrumb("database_query", "Executed SELECT query", "debug");
```

Breadcrumbs appear with the next error for context.

### 4. Context Management

Add custom context to all future events:

```java
tracker.configureScope(scope -> {
    scope.setUser(new User()
        .setId("123")
        .setUsername("john_doe")
        .setEmail("john@example.com"));

    scope.setLevel("info");
    scope.setTag("component", "payment-service");
    scope.setTag("region", "us-west-2");

    Map<String, Object> context = new HashMap<>();
    context.put("version", "1.0.0");
    context.put("api_endpoint", "https://api.example.com");
    scope.setContexts("app", context);
});
```

### 5. Transaction Tracking

Monitor performance and long-running operations:

```java
// Start a transaction
ITransaction transaction = tracker.startTransaction("database_query", "http.server");

try {
    // Query database
    executeQuery();

    // Create a span for detailed tracking
    ISpan span = transaction.startChild("db.query.prepare");
    try {
        prepareStatement();
    } finally {
        span.finish();
    }

    transaction.setStatus("ok");
} catch (Exception e) {
    transaction.setStatus("error");
    throw e;
} finally {
    transaction.finish();
}
```

### 6. Direct Sentry Client Access

For advanced use cases, access the Sentry client directly:

```java
IHub sentryClient = tracker.getSentryClient();

// Use any Sentry API
sentryClient.captureMessage("Custom message");

// Execute custom operations
Hint hint = new Hint();
hint.set("custom_data", "value");
sentryClient.captureException(e, hint);
```

### 7. Connection Management

Ensure all data is sent before shutdown:

```java
// Flush all pending events (2 second timeout by default)
tracker.flush();

// Use before application shutdown
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    tracker.flush();
}));
```

---

## API Reference

### BugTrackerClient

Main SDK facade for error tracking.

#### Initialization

```java
// Using Builder pattern (recommended)
BugTrackerClient tracker = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setEnvironment("production")
    .setRelease("1.0.0")
    .build();

tracker.initialize();

// With auto-detected DSN from environment variable
BugTrackerClient tracker2 = BugTrackerClient.builder()
    .setEnvironment("production")
    .build();

tracker2.initialize(); // Uses SENTRY_DSN environment variable
```

#### Exception Tracking

```java
// Capture exception
tracker.captureException(exception);

// Capture exception with message
tracker.captureException(new RuntimeException("Custom error"), "Context message");

// Capture checked exception
try {
    // code
} catch (IOException e) {
    tracker.captureException(e);
}
```

#### Message Logging

```java
// Capture info message
tracker.captureMessage("User login successful");

// Capture message with level (if available)
tracker.captureMessage("Database connection established");
```

#### Breadcrumb Tracking

```java
// Add breadcrumb with category and message
tracker.addBreadcrumb(
    "user_action",           // category
    "User clicked button",   // message
    "info"                   // level
);

// Breadcrumb appears in next error report
```

#### Context Configuration

```java
// Add context to all future events
tracker.configureScope(scope -> {
    // Set user information
    scope.setUser(new User()
        .setId("user_id")
        .setUsername("username")
        .setEmail("email@example.com"));

    // Set severity level
    scope.setLevel("info");

    // Add tags
    scope.setTag("key", "value");

    // Add custom contexts
    Map<String, Object> data = new HashMap<>();
    data.put("key", "value");
    scope.setContexts("context_name", data);
});
```

#### Transaction Tracking

```java
// Start transaction
ITransaction transaction = tracker.startTransaction(
    "operation_name",    // Operation being tracked
    "operation.type"     // Operation type
);

try {
    // Perform operations
    doWork();
    transaction.setStatus("ok");
} catch (Exception e) {
    transaction.setStatus("error");
    throw e;
} finally {
    transaction.finish();
}
```

#### Span Tracking

```java
ISpan span = transaction.startChild("span_name");
try {
    // Do work
} finally {
    span.finish();
}
```

#### State Management

```java
// Check if initialized
boolean isInit = tracker.isInitialized();

// Flush pending events
tracker.flush();

// Get Sentry client
IHub sentryClient = tracker.getSentryClient();
```

### BugTrackerConfig

Configuration holder for SDK settings.

```java
BugTrackerConfig config = new BugTrackerConfig();

// Getters
String dsn = config.getDSN();
String env = config.getEnvironment();
String release = config.getRelease();
boolean enabled = config.isEnabled();
boolean ignoreErrors = config.isIgnoreErrors();

// Builder
BugTrackerConfig config = new BugTrackerConfig.Builder()
    .setDSN("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setEnvironment("production")
    .setRelease("1.0.0")
    .setServerName("server-1")
    .setEnabled(true)
    .setIgnoreErrors(true)
    .build();
```

---

## Examples

### Example 1: Basic Exception Handling

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

public class BasicExample {
    public static void main(String[] args) {
        // Initialize using builder
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .build();

        tracker.initialize();

        try {
            // Simulate error
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            // Capture exception
            tracker.captureException(e);
        }

        // Ensure all events are sent
        tracker.flush();
    }
}
```

### Example 2: User Context and Tracking

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

public class UserContextExample {
    public static void main(String[] args) {
        // Initialize using builder
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .build();

        tracker.initialize();

        // System user is automatically detected! Override it if needed:
        tracker.context().setUser("user_123", "john@example.com", "john_doe");

        // Add metadata tags
        tracker.context().addTag("environment", "production");
        tracker.context().addTag("service", "payment-service");

        // Track user action
        tracker.breadcrumbs().addBreadcrumb("User initiated payment");

        try {
            // Process payment
            processPayment();
        } catch (Exception e) {
            tracker.captureException(e);
        }

        tracker.flush();
    }

    private static void processPayment() throws Exception {
        // Payment logic
    }
}
```

### Example 3: Transaction Monitoring

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import io.sentry.ITransaction;
import io.sentry.ISpan;

public class TransactionExample {
    public static void main(String[] args) {
        // Initialize using builder
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .setTracesSampleRate(1.0)
            .build();

        tracker.initialize();

        // Start transaction for API call
        ITransaction transaction = tracker.startTransaction(
            "api_request",
            "http.server"
        );

        try {
            // Database query span
            ISpan dbSpan = transaction.startChild("db.query", "SELECT * FROM users");
            try {
                queryDatabase();
            } finally {
                dbSpan.finish();
            }

            // API call span
            ISpan apiSpan = transaction.startChild("http.call", "GET /api/data");
            try {
                callExternalAPI();
            } finally {
                apiSpan.finish();
            }

            transaction.setStatus("ok");
            tracker.captureMessage("API request completed successfully");

        } catch (Exception e) {
            transaction.setStatus("error");
            tracker.captureException(e);
        } finally {
            transaction.finish();
        }

        tracker.flush();
    }

    private static void queryDatabase() throws Exception {
        // Simulate database query
        Thread.sleep(100);
    }

    private static void callExternalAPI() throws Exception {
        // Simulate API call
        Thread.sleep(200);
    }
}
```

### Example 4: Enable/Disable Tracking

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

public class ConfigExample {
    public static void main(String[] args) {
        // Read enabled state from environment
        boolean trackingEnabled = Boolean.parseBoolean(
            System.getenv().getOrDefault("ENABLE_ERROR_TRACKING", "true")
        );

        // Initialize with configuration
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .setEnvironment("production")
            .setEnabled(trackingEnabled)
            .setIgnoreErrors(true)
            .build();

        tracker.initialize();
        // All methods work regardless of enabled state
        try {
            int result = 10 / 0;
        } catch (Exception e) {
            // This is a no-op if tracking is disabled
            tracker.captureException(e);
        }

        tracker.flush();
    }
}
```

### Example 5: Graceful Error Handling

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        // Graceful mode - app continues even if Sentry fails
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .setIgnoreErrors(true)  // default
            .build();

        tracker.initialize();

        try {
            // Even if Sentry is down, these calls won't crash your app
            tracker.captureException(new Exception("Test error"));
            tracker.captureMessage("Test message");
            tracker.breadcrumbs().addBreadcrumb("Test breadcrumb");

            // Your application continues running...
            System.out.println("Application running normally");

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

        tracker.flush();
    }
}
```

### Example 6: Advanced Sentry API Access

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import io.sentry.IHub;
import io.sentry.Hint;

public class AdvancedAccessExample {
    public static void main(String[] args) {
        // Initialize using builder
        BugTrackerClient tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .build();

        tracker.initialize();

        // Get direct access to Sentry client
        IHub sentryClient = tracker.getSentryClient();

        // Use advanced Sentry APIs
        Exception exception = new Exception("Advanced example");

        // Create hint with custom data
        Hint hint = new Hint();
        hint.set("custom_field", "custom_value");

        // Use Sentry API directly
        sentryClient.captureException(exception, hint);

        tracker.flush();
    }
}
```

### Example 7: Web Application Integration

```java
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import io.sentry.ITransaction;

public class WebAppExample {
    private static final BugTrackerClient tracker;

    static {
        tracker = BugTrackerClient.builder()
            .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
            .setTracesSampleRate(1.0)
            .build();
        tracker.initialize();
    }

    public static void handleRequest(String userId, String endpoint) {
        // Start transaction for request
        ITransaction transaction = tracker.startTransaction(
            "http.request",
            "http.server"
        );

        try {
            // Set request context
            tracker.context().addTag("endpoint", endpoint);
            tracker.context().addTag("method", "POST");

            // Set user if available
            if (userId != null) {
                tracker.context().addTag("user_id", userId);
            }

            // Track breadcrumb
            tracker.breadcrumbs().addBreadcrumb("Received " + endpoint);

            // Process request (your code here)
            processRequest(endpoint);

            transaction.setStatus("ok");

        } catch (Exception e) {
            transaction.setStatus("error");
            tracker.captureException(e);
        } finally {
            transaction.finish();
            tracker.flush();
        }
    }

    private static void processRequest(String endpoint) throws Exception {
        // Your request processing logic
    }
}
```

---

## Troubleshooting

### Issue: DSN Not Found

**Problem**: "DSN not provided and SENTRY_DSN environment variable not set"

**Solution**:

```bash
# Set environment variable
export SENTRY_DSN="https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7"

# Or use system property
java -Dsentry.dsn="https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7" MyApp

# Or pass to constructor
BugTrackerClient tracker = new BugTrackerClient("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7");
```

### Issue: ClassNotFoundException

**Problem**: `com.cubetiqs.sdk.bugtracker.BugTrackerClient not found`

**Solution**:

1. Verify JAR is in classpath:

```bash
# For Gradle
./gradlew dependencies | grep bugtracker

# For Maven
mvn dependency:tree | grep bugtracker
```

2. If using manual JAR, ensure it's in the classpath:

```bash
java -cp bugtracker-1.0.1-all.jar:. MyApp
```

### Issue: NoSuchMethodError

**Problem**: NoSuchMethodError on startup

**Solution**: Ensure all dependencies are available. Use shadow JAR which includes all dependencies:

```gradle
implementation 'com.cubetiqs:bugtracker:1.0.1'
```

### Issue: Events Not Sending

**Problem**: Exceptions captured but not appearing in Sentry dashboard

**Solution**:

1. Verify DSN is correct and active
2. Call `flush()` before application exits:

```java
tracker.flush();
```

3. Check network connectivity
4. Verify Sentry project allows inbound events
5. Check firewall/proxy settings

### Issue: Performance Impact

**Problem**: Application running slower with error tracking

**Solution**:

1. Use `ignoreErrors=true` (default) for better resilience
2. Disable tracking in development if not needed:

```java
.setEnabled(System.getenv("DEV_MODE") == null)
```

3. Use async initialization
4. Limit breadcrumbs if tracking too many

### Issue: OutOfMemoryError

**Problem**: Memory usage increases over time

**Solution**:

1. Limit breadcrumb accumulation
2. Clear scope regularly:

```java
tracker.configureScope(scope -> {
    scope.clear();
});
```

3. Monitor transaction count
4. Ensure `flush()` is called periodically

---

## FAQ

### Q: What Java versions are supported?

**A**: BugTracker supports Java 1.8 through Java 21. Compatibility is maintained through careful dependency selection.

### Q: Can I use BugTracker without Sentry?

**A**: BugTracker requires a Sentry instance. You can self-host Sentry or use Sentry.io's managed service.

### Q: Is BugTracker thread-safe?

**A**: Yes, BugTracker is thread-safe. All internal operations are synchronized.

### Q: Can I disable tracking at runtime?

**A**: Yes, use `setEnabled(false)` during initialization or environment variables:

```bash
export ENABLE_TRACKING=false
```

### Q: How often should I call flush()?

**A**: Call `flush()` when your application is shutting down to ensure all events are sent. For long-running applications, you can call it periodically but it's not required.

### Q: Can I use multiple BugTrackerClient instances?

**A**: Yes, but it's not recommended. Use a single shared instance:

```java
public class App {
    public static final BugTrackerClient tracker = new BugTrackerClient(...);
}
```

### Q: How do I set environment-specific configuration?

**A**: Use environment variables:

```java
BugTrackerClient tracker = new BugTrackerClient.Builder()
    .setEnvironment(System.getenv("APP_ENV"))
    .setRelease(System.getenv("APP_VERSION"))
    .build();
```

### Q: What happens if Sentry is down?

**A**: With `ignoreErrors=true` (default), the application continues normally. With `ignoreErrors=false`, errors propagate.

### Q: Can I send custom data to Sentry?

**A**: Yes, use `configureScope()` or access the Sentry client directly:

```java
tracker.configureScope(scope -> {
    scope.setTag("custom_tag", "value");
    Map<String, Object> data = new HashMap<>();
    data.put("key", "value");
    scope.setContexts("custom", data);
});
```

### Q: How do I test if BugTracker is working?

**A**:

```java
BugTrackerClient tracker = new BugTrackerClient("your-dsn");

try {
    throw new Exception("Test error");
} catch (Exception e) {
    tracker.captureException(e);
}

tracker.flush();
System.out.println("Check your Sentry dashboard!");
```

### Q: Is BugTracker open source?

**A**: Yes, BugTracker is an open-source project. Visit the [GitHub repository](https://github.com/CUBETIQ/bugtracker-java) for source code and issues.

### Q: How do I report bugs?

**A**: Open an issue on the [GitHub repository](https://github.com/CUBETIQ/bugtracker-java/issues).

### Q: Can I contribute?

**A**: Yes! Pull requests are welcome. Please follow the contribution guidelines in the repository.

---

## Getting Help

-   **Documentation**: See this file and code examples
-   **GitHub Issues**: [Report bugs or request features](https://github.com/CUBETIQ/bugtracker-java/issues)
-   **GitHub Discussions**: [Ask questions and discuss](https://github.com/CUBETIQ/bugtracker-java/discussions)
-   **Sentry Docs**: [Sentry Java SDK documentation](https://docs.sentry.io/platforms/java/)

---

## License

BugTracker Java SDK is licensed under the MIT License. See LICENSE file for details.

---

**Last Updated**: November 2025
**Version**: 1.0.1
