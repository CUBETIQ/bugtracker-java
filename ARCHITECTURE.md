# BugTracker SDK Architecture

## Project Overview

BugTracker is a high-performance, flexible Java SDK that wraps Sentry for error tracking, monitoring, and debugging of Java applications. It provides a developer-friendly API with best practices built-in.

## Core Design Principles

1. **Flexibility** - Multiple ways to configure and use the SDK
2. **Performance** - Minimal overhead with asynchronous event transmission
3. **Developer Experience** - Fluent APIs and sensible defaults
4. **Maintainability** - Clean separation of concerns, well-documented code
5. **Type Safety** - Leverages Java's type system for compile-time safety

## Architecture

### Package Structure

```
com.cubetiqs.sdk.bugtracker/
├── BugTrackerClient.java              # Main entry point and facade
├── BugTrackerConfig.java              # Configuration management
│
├── breadcrumb/
│   ├── BreadcrumbBuilder.java         # Fluent builder for breadcrumbs
│   └── BreadcrumbManager.java         # Breadcrumb management and convenience methods
│
├── context/
│   └── ContextManager.java            # User/tags/extras management
│
├── event/
│   └── EventBuilder.java              # Event construction helper
│
├── exception/
│   └── BugTrackerException.java       # SDK-specific exceptions
│
├── hook/
│   ├── BugTrackerHook.java            # Event lifecycle interface
│   └── HookManager.java               # Hook execution chain
│
├── logger/
│   └── BugTrackerLogHandler.java      # JUL integration
│
├── transaction/
│   └── TransactionManager.java        # Performance monitoring
│
└── examples/
    └── BugTrackerExamples.java        # Example usage patterns
```

## Key Components

### 1. BugTrackerClient (Main Facade)

```
┌─────────────────────────────┐
│   BugTrackerClient          │
├─────────────────────────────┤
│ - initialize()              │
│ - captureException()        │
│ - captureMessage()          │
│ - breadcrumbs()             │ ──→ BreadcrumbManager
│ - context()                 │ ──→ ContextManager
│ - hooks()                   │ ──→ HookManager
│ - configureScope()          │
│ - flush()                   │
│ - close()                   │
└─────────────────────────────┘
        │
        └─→ Sentry SDK
```

**Responsibilities:**
- Lifecycle management (initialize, close)
- Event capture orchestration
- Manager instance provisioning
- Configuration delegation

### 2. BreadcrumbManager

Provides both low-level and high-level breadcrumb tracking:

```
BreadcrumbManager
├── Generic Methods
│   ├── addBreadcrumb(String)
│   ├── addBreadcrumb(String, SentryLevel)
│   └── addBreadcrumb(BreadcrumbBuilder)
└── Convenience Methods
    ├── http(method, url, statusCode)
    ├── database(query)
    ├── userAction(action)
    ├── error(message, errorType)
    ├── warning(message)
    └── debug(message)
```

### 3. ContextManager

Manages scope-level context information:

```
ContextManager
├── User Management
│   ├── setUser(User)
│   ├── setUser(userId)
│   ├── setUser(userId, email, username)
│   └── clearUser()
├── Tag Management
│   ├── addTag(key, value)
│   └── addTags(Map)
├── Extra Management
│   ├── addExtra(key, value)
│   └── addExtras(Map)
└── Clear
    └── clearContext()
```

### 4. EventBuilder

Fluent builder for constructing events:

```
EventBuilder
├── Configuration
│   ├── addTag(key, value)
│   ├── addTags(Map)
│   ├── addExtra(key, value)
│   ├── addExtras(Map)
│   └── withLevel(SentryLevel)
├── Advanced
│   ├── withException(Throwable)
│   └── withScopeConfigurator(Consumer<IScope>)
└── Accessors
    ├── getMessage()
    ├── getLevel()
    ├── getTags()
    ├── getExtras()
    └── getException()
```

### 5. HookManager

Event transformation and filtering pipeline:

```
┌──────────────┐
│  SentryEvent │
└──────────────┘
      │
      ↓
┌─────────────────────────────┐
│    HookManager              │
│  ┌───────────────────────┐  │
│  │ Hook 1                │  │
│  │ - Transform event     │  │
│  │ - Add tags            │  │
│  └───────────────────────┘  │
│            ↓                │
│  ┌───────────────────────┐  │
│  │ Hook 2                │  │
│  │ - Redact data         │  │
│  │ - Filter events       │  │
│  └───────────────────────┘  │
│            ↓                │
│  ┌───────────────────────┐  │
│  │ Hook N                │  │
│  │ - Custom logic        │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
      │
      ↓ (modified or null)
   Sentry
```

### 6. BugTrackerLogHandler

Integrates Java Util Logging:

```
java.util.logging
      │
      ↓
┌──────────────────────────┐
│ BugTrackerLogHandler     │
├──────────────────────────┤
│ - Converts JUL Levels   │
│   to SentryLevel        │
│ - Extracts context      │
│ - Handles exceptions    │
└──────────────────────────┘
      │
      ↓
 BugTrackerClient
      │
      ↓
    Sentry
```

### 7. TransactionManager

Performance monitoring wrapper:

```
TransactionManager
├── Transaction Lifecycle
│   ├── start(name, operation)
│   ├── startChild(operation)
│   ├── startChild(operation, description)
│   ├── finish()
│   └── close() (AutoCloseable)
└── Accessors
    ├── getName()
    ├── getOperation()
    └── getTransaction()
```

## Data Flow

### Exception Capture Flow

```
try/catch block
      │
      ↓
BugTrackerClient.captureException()
      │
      ├─→ Ensure initialized
      │
      ├─→ Apply scope configurator (if provided)
      │
      ├─→ Sentry.captureException()
      │
      └─→ Execute HookManager chain
           ├─→ Hook 1
           ├─→ Hook 2
           ├─→ Hook N
           │
           └─→ Send to Sentry API
                    │
                    ↓
                  Dashboard
```

### Message Capture Flow

```
User Code
   │
   ↓
captureMessage(message, level)
   │
   ├─→ Validate & Initialize
   │
   ├─→ Create SentryEvent
   │
   ├─→ Apply current context (scope)
   │   ├─→ Current user
   │   ├─→ Tags
   │   └─→ Extras
   │
   ├─→ Apply default tags
   │
   ├─→ Execute hooks
   │   ├─→ Transformation
   │   ├─→ Filtering
   │   └─→ Redaction
   │
   └─→ Sentry.captureMessage()
        │
        └─→ Async transmission to Sentry
```

## Configuration Hierarchy

```
Default Values
      ↓
BugTrackerConfig Defaults (hardcoded DSN)
      ↓
System Properties (bugtracker.sentry.*)
      ↓
Environment Variables (BUGTRACKER_*, SENTRY_*)
      ↓
Builder Configuration (explicit setters)
      ↓
Final Configuration
```

## Thread Safety

- **Thread-Safe**: All public methods are thread-safe
- **Scope Isolation**: Context is managed per thread via Sentry's scope mechanism
- **Breadcrumbs**: Breadcrumbs are isolated per thread
- **Initialization**: Double-checked locking for single initialization

```
BugTrackerClient
├── Synchronized Block
│   └─→ Initialize Sentry only once
└── Method-level Thread Safety
    ├─→ captureException (thread-safe)
    ├─→ captureMessage (thread-safe)
    ├─→ addBreadcrumb (thread-safe)
    └─→ configureScope (thread-safe)
```

## Performance Characteristics

| Operation | Latency | Notes |
|-----------|---------|-------|
| captureException | < 1ms | Async transmission |
| captureMessage | < 1ms | Async transmission |
| addBreadcrumb | < 0.1ms | In-memory storage |
| setTag | < 0.1ms | Scope update |
| setUser | < 0.1ms | Scope update |
| flush(5s) | ~5s | Waits for transmission |
| initialize | ~100ms | First call slower |

## Sentry Integration Points

```
BugTrackerClient
├─→ Sentry.init(options)           [Configuration]
├─→ Sentry.isEnabled()             [Status check]
├─→ Sentry.captureException()      [Exception capture]
├─→ Sentry.captureMessage()        [Message capture]
├─→ Sentry.addBreadcrumb()         [Breadcrumb]
├─→ Sentry.configureScope()        [Context]
├─→ Sentry.withScope()             [Scoped context]
├─→ Sentry.flush()                 [Flush events]
├─→ Sentry.close()                 [Shutdown]
└─→ Sentry.startTransaction()      [Performance]
```

## Error Handling

- **Initialization Errors**: Logged but don't throw (fail-safe)
- **Capture Errors**: Silently ignored to avoid breaking application
- **Hook Errors**: Gracefully handled, next hook continues
- **Flush Timeout**: Returns after timeout (doesn't wait indefinitely)

## Testing Strategy

1. **Unit Tests**: Each component tested in isolation
   - EventBuilder: 5 tests
   - ContextManager: 5 tests
   - HookManager: 4 tests
   - BugTrackerClient: 6 tests

2. **Integration Tests**: Cross-component scenarios
3. **Example Code**: Demonstrates real-world usage

## Extensibility Points

1. **Custom Hooks**: Implement `BugTrackerHook` for event transformation
2. **Custom Loggers**: Implement handler extending `BugTrackerLogHandler`
3. **Custom Configuration**: Extend `BugTrackerConfig` for specialized setups
4. **Custom Managers**: Create managers wrapping core functionality

## Deployment Considerations

- **No External Dependencies** (except Sentry SDK)
- **Graceful Degradation**: Works even if Sentry is unavailable
- **Memory**: Minimal footprint (~2MB JAR)
- **Classloading**: No conflicts with other Sentry consumers
