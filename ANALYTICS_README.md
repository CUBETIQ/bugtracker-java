# Cubis Analytics Client for Java

High-performance, non-blocking analytics client for Umami platform. Built for reliability, performance, and zero-error operation.

## Features

- ✅ **Non-blocking**: Events queued immediately, processed asynchronously
- ✅ **Retryable**: Automatic retry with exponential backoff
- ✅ **High Performance**: Bounded queue, daemon worker threads
- ✅ **Reliable**: Never throws exceptions to application code
- ✅ **No Errors**: Graceful degradation when analytics server is down
- ✅ **Thread-Safe**: Safe for use in multi-threaded applications
- ✅ **Zero Dependencies**: Uses only JDK HttpURLConnection

## Quick Start

### Basic Usage

```java
import com.cubetiqs.sdk.analytics.CubisAnalyticsClient;

// Create and initialize client
CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
        "https://analytics.ctdn.dev",
        "your-website-id"
    )
    .setEnabled(true)
    .build();

analytics.initialize();

// Track events
analytics.track("page-view", "/", "Home Page");
analytics.track("button-click", "/checkout", "Checkout Button");

// Cleanup
analytics.close();
```

### With Custom Data

```java
import java.util.Map;

Map<String, Object> data = Map.of(
    "category", "user",
    "value", 100,
    "currency", "USD"
);

analytics.track("purchase", "/confirmation", "Order Complete", data);
```

### Try-with-Resources (Recommended)

```java
try (CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(url, websiteId).build()) {
    analytics.initialize();

    analytics.track("event", "/page", "Title");

} // Automatically flushes and closes
```

## Advanced Configuration

```java
import java.time.Duration;

CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(url, websiteId)
    .setEnabled(true)                                    // Enable/disable tracking
    .setMaxQueueSize(5000)                               // Custom queue size
    .setMaxRetries(5)                                    // More retry attempts
    .setInitialRetryDelay(Duration.ofMillis(100))        // Retry delay
    .setMaxRetryDelay(Duration.ofSeconds(30))            // Max backoff delay
    .setRequestTimeout(Duration.ofSeconds(10))           // HTTP timeout
    .setWorkerThreads(2)                                 // Worker threads
    .setDebugEnabled(true)                               // Debug logging
    .build();
```

## Custom Event Tracking

```java
import com.cubetiqs.sdk.analytics.event.EventPayload;

EventPayload.Builder payloadBuilder = EventPayload.builder(websiteId, "custom-event")
    .setHostname("example.com")
    .setScreen("1920x1080")
    .setLanguage("en-US")
    .setUrl("/custom-page")
    .setReferrer("https://google.com")
    .setTitle("Custom Page")
    .setTag("important")
    .addData("user_id", "12345")
    .addData("plan", "premium")
    .addData("value", 99.99);

analytics.track(payloadBuilder);
```

## Monitoring & Statistics

```java
// Check queue health
int queueSize = analytics.getQueueSize();
long queued = analytics.getEventsQueued();
long processed = analytics.getEventsProcessed();
long dropped = analytics.getEventsDropped();

System.out.println("Queue: " + queueSize +
                   ", Processed: " + processed +
                   ", Dropped: " + dropped);

// Flush before shutdown
analytics.flush(10, TimeUnit.SECONDS);
```

## Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `url` | *required* | Umami server URL |
| `websiteId` | *required* | Website identifier |
| `enabled` | `true` | Enable/disable tracking |
| `maxQueueSize` | `10000` | Max events in queue |
| `maxRetries` | `3` | Retry attempts |
| `initialRetryDelay` | `500ms` | First retry delay |
| `maxRetryDelay` | `30s` | Max backoff delay |
| `requestTimeout` | `10s` | HTTP timeout |
| `workerThreads` | `1` | Worker threads |
| `debugEnabled` | `false` | Debug logging |

## E-commerce Example

```java
// Product view
analytics.track("product-view", "/products/widget", "Premium Widget",
    Map.of(
        "product_id", "SKU-123",
        "price", 49.99,
        "currency", "USD"
    )
);

// Add to cart
analytics.track("add-to-cart", "/cart", "Cart",
    Map.of(
        "product_id", "SKU-123",
        "quantity", 2,
        "total", 99.98
    )
);

// Purchase
analytics.track("purchase", "/confirmation", "Order Complete",
    Map.of(
        "order_id", "ORD-67890",
        "revenue", 109.97,
        "items_count", 2
    )
);
```

## Background Service Example

```java
CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(url, websiteId)
    .setWorkerThreads(2)
    .setMaxQueueSize(10000)
    .build();

analytics.initialize();

// Add shutdown hook for graceful cleanup
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    System.out.println("Shutting down analytics...");
    analytics.flush(10, TimeUnit.SECONDS);
    analytics.close();
}));

// Your background service logic...
```

## Performance Characteristics

| Operation | Latency | Notes |
|-----------|---------|-------|
| `track()` | < 0.5ms | Non-blocking |
| `enqueue()` | < 0.1ms | Immediate return |
| HTTP send | Variable | With retry |
| Retry delay | 500ms - 30s | Exponential backoff |

**Scalability:**
- Default queue: 10,000 events
- Memory per event: ~1KB
- Max memory: ~10MB (default config)

## Error Handling

The client is designed to **never cause application failures**:

- All exceptions are caught and logged (if debug enabled)
- Failed sends are retried automatically
- After max retries, events are dropped silently
- Queue full: New events dropped (FIFO)
- Analytics server down: Events queued and retried

## Thread Safety

- All public methods are thread-safe
- Uses `LinkedBlockingQueue` for thread-safe operations
- Worker threads run in daemon mode
- Safe for concurrent use from multiple threads

## Umami Integration

The client sends events to Umami's `/api/send` endpoint:

```json
{
  "type": "event",
  "payload": {
    "hostname": "example.com",
    "language": "en-US",
    "referrer": "",
    "screen": "1920x1080",
    "title": "Page Title",
    "url": "/page",
    "website": "your-website-id",
    "name": "event-name",
    "data": {
      "custom": "data"
    }
  }
}
```

No authentication required. Proper `User-Agent` header automatically included.

## Best Practices

1. **Initialize once**: Create one client instance per application
2. **Use try-with-resources**: Ensures proper cleanup
3. **Flush before shutdown**: Give events time to send
4. **Monitor statistics**: Track queue size and dropped events
5. **Enable debug during development**: Helps troubleshoot issues
6. **Disable in tests**: Set `enabled(false)` for unit tests

## Troubleshooting

### Events not being sent?

1. Check if initialized: `analytics.isInitialized()`
2. Verify enabled: `analytics.isEnabled()`
3. Enable debug mode: `.setDebugEnabled(true)`
4. Check network connectivity to Umami server
5. Verify URL and website ID are correct

### High queue size?

1. Check if analytics server is responding
2. Increase worker threads: `.setWorkerThreads(2)`
3. Reduce event volume
4. Check for network issues

### Events being dropped?

1. Check `getEventsDropped()` count
2. Increase queue size: `.setMaxQueueSize(20000)`
3. Add more worker threads
4. Reduce event generation rate

## Examples

See `AnalyticsExamples.java` for complete examples:
- Basic usage
- Advanced configuration
- E-commerce tracking
- Web application tracking
- Background service integration

## License

MIT License - Part of BugTracker SDK

## Support

- Email: oss@cubetiqs.com
- Issues: https://github.com/cubetiq/bugtracker-java/issues
