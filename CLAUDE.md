# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This SDK provides two main components:

1. **BugTracker SDK** - A flexible, high-performance wrapper around Sentry (v7.10.0) for error tracking, monitoring, and debugging
2. **Cubis Analytics Client** - A high-performance analytics client for Umami platform with non-blocking event tracking

**Key Characteristics:**
- Java 1.8 compatible (supports through Java 21+)
- Thread-safe with scope isolation
- Async event transmission with minimal overhead
- Clean separation of concerns with manager pattern
- Fail-safe design: never causes application failures

## Build Commands

### Standard Build & Test
```bash
./gradlew build         # Full build with tests
./gradlew test          # Run tests only
./gradlew clean build   # Clean build
```

### Shadow JAR (with dependencies bundled)
```bash
./gradlew shadowJar     # Creates bugtracker-{version}-all.jar
```

**Note:** Shadow JAR relocates dependencies to avoid conflicts:
- `io.sentry` → `com.cubetiqs.sdk.internal.sentry`
- `org.slf4j` → `com.cubetiqs.sdk.internal.slf4j`
- `ch.qos.logback` → `com.cubetiqs.sdk.internal.logback`
- `com.google.common` → `com.cubetiqs.sdk.internal.guava`

### Convenience Scripts
```bash
./scripts/build.sh                  # Standard build
./scripts/build.sh --shadow         # Build with shadow JAR
./scripts/build.sh --test-only      # Run tests only
./scripts/build.sh --clean-cache    # Clean cache and rebuild
```

### Release
```bash
./scripts/release.sh                # Interactive release (recommended)
./scripts/release.sh -v 1.2.0       # Release specific version
./scripts/release.sh --dry-run      # Preview without changes
```

## Architecture

### Package Structure & Responsibilities

**Core Components:**
- `BugTrackerClient` - Main facade providing lifecycle management, event capture orchestration, and manager provisioning
- `BugTrackerConfig` - Configuration management with hierarchy: defaults → system properties → environment variables → builder

**Manager Pattern (accessed via facade):**
- `BreadcrumbManager` (`client.breadcrumbs()`) - Breadcrumb tracking with category-specific helpers (http, database, userAction, error)
- `ContextManager` (`client.context()`) - User/tags/extras management with thread-local scope
- `HookManager` (`client.hooks()`) - Event transformation pipeline for filtering/redacting before Sentry transmission
- `TransactionManager` - Performance monitoring with nested span support (implements AutoCloseable)

**Builders:**
- `BreadcrumbBuilder` - Fluent breadcrumb construction with category, level, and data
- `EventBuilder` - Event construction with tags, extras, level, and exception support

**Integration:**
- `BugTrackerLogHandler` - Java Util Logging (JUL) integration that converts log levels to Sentry levels

### Analytics Client Architecture

**Package: `com.cubetiqs.sdk.analytics`**

**Core Components:**
- `CubisAnalyticsClient` - Main facade for Umami analytics tracking with non-blocking operations, session management, and user identification
- `CubisAnalyticsConfig` - Configuration with required fields (url, websiteId) and optional settings (enabled, retry, queue size, etc.)

**Event Models:**
- `EventPayload` - Umami API payload with fields: hostname, screen, language, url, referrer, title, tag, id, website, name (optional for pageviews), data
- `AnalyticsEvent` - Event wrapper with payload, type, timestamp, and retry tracking

**Internal Components:**
- `EventQueue` - Bounded blocking queue with worker threads for async processing (prevents memory issues)
- `EventSender` - HTTP sender with exponential backoff retry logic (uses Unirest HTTP client)
- `JsonSerializer` - Lightweight JSON serialization without external libraries
- `UserAgentBuilder` - Dynamic User-Agent builder based on system properties (OS, version, architecture)

**Key Features:**
- **Non-blocking**: Events queued immediately, processed by background worker threads
- **Retryable**: Exponential backoff with configurable max retries (default: 3)
- **High Performance**: Bounded queue (default: 10,000), daemon worker threads
- **Reliable**: All exceptions caught and logged, never throws to application
- **No Errors**: Graceful degradation when analytics server is down or slow
- **Session Management**: Persistent session ID per client instance for event correlation
- **User Identification**: Support for user tracking with login/logout (identify/clearIdentity)
- **Automatic Cache Management**: Cache is automatically cleared when user identity changes to ensure fresh tracking
- **Dynamic User-Agent**: Auto-detects OS, version, and architecture for realistic browser emulation
- **Pageview vs Events**: Separate methods for pageviews (Overview stats) and custom events (Events tab)

**Event Flow:**
```
# Pageview tracking (for Overview/Statistics)
trackPageView() → validate enabled/initialized → build EventPayload (name=null)
→ use currentUserId if set, else sessionId → enqueue() [non-blocking return]

# Custom event tracking (for Events tab)
track() → validate enabled/initialized → build EventPayload (with name)
→ use currentUserId if set, else sessionId → enqueue() [non-blocking return]

# User identification (login)
identify() → clear cache → set currentUserId → send identify event → all subsequent events use userId

# Clear identity (logout)
clearIdentity() → clear cache → clear currentUserId → subsequent events revert to sessionId

# Common processing path
→ EventQueue [bounded queue] → Worker Thread polls
→ EventSender.send() → HTTP POST to /api/send with cache header
→ Retry with exponential backoff on failure → Extract cache from response
→ Update statistics
```

**Configuration Options:**
- `url` (required): Umami server URL
- `websiteId` (required): Website identifier in Umami
- `enabled` (default: true): Enable/disable tracking
- `maxQueueSize` (default: 10,000): Prevent memory issues
- `maxRetries` (default: 3): Retry attempts for failed requests
- `initialRetryDelay` (default: 500ms): First retry delay
- `maxRetryDelay` (default: 30s): Max delay for exponential backoff
- `requestTimeout` (default: 10s): HTTP request timeout
- `workerThreads` (default: 1): Background processing threads
- `debugEnabled` (default: false): Enable debug logging

### Critical Data Flows

**Exception Capture:**
```
User code → captureException() → validate initialized → apply scope configurator
→ Sentry.captureException() → HookManager chain → Sentry API (async)
```

**Message Capture:**
```
captureMessage() → create SentryEvent → apply context (user/tags/extras)
→ apply default tags → execute hooks → Sentry.captureMessage() → async transmission
```

### Thread Safety

- All public methods are thread-safe
- Context is managed per thread via Sentry's scope mechanism
- Breadcrumbs are thread-isolated
- Initialization uses double-checked locking for single initialization

## Testing

**Test Structure:**
- Unit tests for each component in `src/test/java/`
- BugTracker examples in `BugTrackerExamples.java`
- Analytics examples in `AnalyticsExamples.java`

**Test Coverage:**
- BugTracker: EventBuilder (5), ContextManager (5), HookManager (4), BugTrackerClient (6)
- Analytics: CubisAnalyticsClient (18), EventPayload (11), JsonSerializer (7)

**Running Tests:**
```bash
./gradlew test                                       # All tests
./gradlew test --tests "ClassName"                   # Single test class
./gradlew test --tests "*.MethodName"                # Specific test method
./gradlew test --tests "com.cubetiqs.sdk.analytics.*" # Analytics tests only
./gradlew test --tests "com.cubetiqs.sdk.bugtracker.*" # BugTracker tests only
./gradlew test --info                                # Verbose output
```

## Configuration Hierarchy

Configuration is resolved in this order (later overrides earlier):
1. Default values (hardcoded DSN: `https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7`)
2. System properties (`bugtracker.sentry.*`)
3. Environment variables (`BUGTRACKER_*`, `SENTRY_*`)
4. Builder configuration (explicit setters)

**Common Configuration:**
- DSN: Required for Sentry connection
- Environment: `production`, `staging`, `development`
- Release: Version string for tracking
- Sample Rate: 0.0-1.0 (percentage of events to send)
- Traces Sample Rate: 0.0-1.0 (percentage of performance events)

## Code Style & Patterns

### Builder Pattern Usage
All major components use builder pattern for construction:
```java
BugTrackerClient client = BugTrackerClient.builder()
    .setDsn("...")
    .setEnvironment("production")
    .build();
```

### Manager Pattern
Managers are accessed through the main client facade and provide focused functionality:
```java
client.breadcrumbs().http("GET", "/api/users", 200);
client.context().setUser(userId, email, username);
client.hooks().addHook((event, hint) -> modifiedEvent);
```

### Try-with-Resources
`BugTrackerClient` and `TransactionManager` implement AutoCloseable:
```java
try (BugTrackerClient tracker = BugTrackerClient.builder().build()) {
    tracker.initialize();
    // Use tracker
} // Automatically closes and flushes
```

### Scope Configurators
Lambda-based scope configuration for per-event context:
```java
tracker.captureException(e, scope -> {
    scope.setTag("order_id", orderId);
    scope.setExtra("customer", customerId);
});
```

## Common Development Tasks

### BugTracker SDK

#### Adding New Breadcrumb Categories
1. Add method to `BreadcrumbManager` with appropriate category string
2. Use `BreadcrumbBuilder` for construction
3. Add corresponding test case
4. Document in README.md

#### Adding New Event Hooks
1. Implement `BugTrackerHook` interface
2. Register via `client.hooks().addHook()`
3. Return modified event or null to drop
4. Hooks execute in registration order

#### Extending Configuration
1. Add property to `BugTrackerConfig`
2. Add builder method in `BugTrackerClient.Builder`
3. Apply to Sentry options in `initializeSentry()`
4. Document in configuration section

### Analytics Client

#### Basic Usage Pattern
```java
// Initialize
CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(url, websiteId)
    .setEnabled(true)
    .build();
analytics.initialize();

// Track pageviews (appear in Umami Overview/Statistics)
analytics.trackPageView("/", "Home Page");
analytics.trackPageView("/about", "About Page");

// Track custom events (appear in Umami Events tab)
analytics.track("button-click", "/page-url", "Page Title");
analytics.track("event-with-data", "/page", "Title", Map.of("key", "value"));

// User identification (for login scenarios)
Map<String, Object> userData = Map.of("name", "John", "email", "john@example.com");
analytics.identify("user-123", userData);

// All subsequent events will use user-123 as ID
analytics.trackPageView("/dashboard", "Dashboard");

// Clear identity (for logout scenarios)
analytics.clearIdentity();

// Cleanup
analytics.close();
```

**Important Distinction:**
- **Pageviews** (`trackPageView`): No event name, shows in Overview/Statistics, counts as page visits
- **Custom Events** (`track`): Has event name, shows in Events tab, tracks specific actions

#### Adding Custom Event Tracking
1. Build `EventPayload` with required and optional fields
2. Track using `client.track(EventPayload.Builder)`
3. Custom data goes in the `data` field as Map<String, Object>
4. All tracking methods are non-blocking

#### Monitoring Analytics Health
```java
// Check queue and processing stats
int queueSize = analytics.getQueueSize();
long queued = analytics.getEventsQueued();
long processed = analytics.getEventsProcessed();
long dropped = analytics.getEventsDropped();

// Flush before shutdown
analytics.flush(10, TimeUnit.SECONDS);
```

#### Debugging Analytics Issues
1. Enable debug mode: `.setDebugEnabled(true)`
2. Check if initialized: `analytics.isInitialized()`
3. Verify enabled: `analytics.isEnabled()`
4. Monitor queue size to detect backlog
5. Check dropped events count for capacity issues

## CI/CD & Release Process

**GitHub Actions Workflows:**
- `ci.yml` - Runs on push/PR to main/develop (tests on Java 11, 17, 21)
- `pr-validation.yml` - PR validation with status comments
- `release.yml` - Triggered by version tags (v*) for automated releases

**Creating a Release:**
1. Use `./scripts/release.sh` for interactive release
2. Script handles: version updates, changelog generation, git tagging, pushing
3. GitHub Actions automatically: runs tests, builds JARs, creates release, uploads artifacts

**Version Format:**
- Use semantic versioning: X.Y.Z
- Git tags: vX.Y.Z (e.g., v1.2.0)
- Major: Breaking changes
- Minor: New features (backwards compatible)
- Patch: Bug fixes

## Dependencies & Compatibility

**Java Version Support:**
- Minimum: Java 1.8
- Tested on: Java 8, 11, 17, 21
- Source/target compatibility: Java 1.8

**Key Dependencies:**
- Sentry: 7.10.0 (7.x series for Java 1.8 compatibility, 8.x requires Java 11)
- SLF4J: 1.7.36 (1.7.x for Java 1.8, 2.x requires Java 9+)
- Logback: 1.2.13 (for logging)
- Guava: 30.1.1-jre (30.x for Java 1.8, 32.x requires Java 9+)

**Test Dependencies:**
- JUnit Jupiter: 5.9.3
- Mockito: 4.11.0

## Error Handling Philosophy

- **Initialization errors**: Logged but don't throw (fail-safe approach)
- **Capture errors**: Silently ignored to avoid breaking application flow
- **Hook errors**: Gracefully handled, next hook continues execution
- **Flush timeout**: Returns after timeout without blocking indefinitely

This design ensures the SDK never causes application failures, even when Sentry is unavailable.

## Performance Characteristics

### BugTracker SDK

| Operation | Latency | Notes |
|-----------|---------|-------|
| captureException | < 1ms | Async transmission |
| captureMessage | < 1ms | Async transmission |
| addBreadcrumb | < 0.1ms | In-memory storage |
| setTag | < 0.1ms | Scope update |
| setUser | < 0.1ms | Scope update |
| flush(5s) | ~5s | Waits for transmission |
| initialize | ~100ms | First call slower |

### Analytics Client

| Operation | Latency | Notes |
|-----------|---------|-------|
| track() | < 0.5ms | Non-blocking, immediate return |
| enqueue() | < 0.1ms | Offer to bounded queue |
| EventSender.send() | Variable | HTTP POST with retry |
| Retry delay | 500ms - 30s | Exponential backoff |
| Worker thread poll | 1s timeout | Allows graceful shutdown |
| initialize | < 10ms | Thread pool creation |
| flush() | Variable | Waits for queue to empty |
| shutdown() | < 10s | Worker thread termination |

**Scalability:**
- Default queue size: 10,000 events (configurable)
- Queue full behavior: Drops oldest events (no blocking)
- Worker threads: 1 by default (configurable up to CPU cores)
- Memory per event: ~1KB (approximate)
- Max memory footprint: ~10MB for full queue (default config)

## System User Auto-Detection

The SDK automatically detects the system user for default context:
- Uses system properties: `user.name` and `user.home`
- Fallback to "unknown" if not available
- Can be overridden with explicit `context().setUser()` call

## Analytics Client Implementation Notes

### Umami API Integration
- Endpoint: `POST /api/send`
- No authentication required
- Requires proper `User-Agent` header (automatically set)
- Response includes: `cache`, `sessionId`, `visitId`

### Cache Management
- **Cache Header**: `x-umami-cache` header sent with each request for visitor tracking
- **Cache Extraction**: Response cache value is extracted and stored for subsequent requests
- **Automatic Clearing**: Cache is automatically cleared when user identity changes via:
  - `identify(userId, userData)` - Clear cache when new user logs in
  - `clearIdentity()` - Clear cache when user logs out
- **Purpose**: Ensures Umami can properly track new visitors/users when identity changes
- **Importance**: Without cache clearing, new users would be tracked with the previous user's cache, causing incorrect analytics

### Error Handling Strategy
- **Never throws exceptions** to calling code
- All exceptions caught and logged (if debug enabled)
- Failed sends are retried with exponential backoff
- After max retries, events are dropped silently
- Queue full: New events dropped (FIFO behavior)

### Thread Safety
- All public methods are thread-safe
- EventQueue uses `LinkedBlockingQueue` for thread-safe operations
- Worker threads use daemon mode (won't prevent JVM shutdown)
- Statistics use `AtomicLong` for lock-free updates

### Shutdown Behavior
- `close()` or `shutdown()` stops accepting new events
- Existing queued events are processed (up to timeout)
- Worker threads terminated gracefully
- Pending events flushed with configurable timeout
- Safe to call multiple times (idempotent)
