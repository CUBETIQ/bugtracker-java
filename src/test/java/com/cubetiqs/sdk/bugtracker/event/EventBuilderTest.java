package com.cubetiqs.sdk.bugtracker.event;

import io.sentry.SentryLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventBuilder Tests")
class EventBuilderTest {

    @Test
    @DisplayName("Should create EventBuilder with message")
    void testCreation() {
        EventBuilder builder = new EventBuilder("Test message");
        assertEquals("Test message", builder.getMessage());
        assertEquals(SentryLevel.INFO, builder.getLevel());
    }

    @Test
    @DisplayName("Should add tags fluently")
    void testAddTags() {
        EventBuilder builder = new EventBuilder("Test")
                .addTag("environment", "test")
                .addTag("service", "auth");

        Map<String, String> tags = builder.getTags();
        assertEquals(2, tags.size());
        assertEquals("test", tags.get("environment"));
        assertEquals("auth", tags.get("service"));
    }

    @Test
    @DisplayName("Should add extras fluently")
    void testAddExtras() {
        EventBuilder builder = new EventBuilder("Test")
                .addExtra("user_id", 123)
                .addExtra("request_id", "abc123");

        Map<String, Object> extras = builder.getExtras();
        assertEquals(2, extras.size());
    }

    @Test
    @DisplayName("Should support exception")
    void testWithException() {
        Throwable ex = new RuntimeException("Test error");
        EventBuilder builder = new EventBuilder("Error").withException(ex);
        assertEquals(ex, builder.getException());
    }

    @Test
    @DisplayName("Should reject null message")
    void testNullMessageRejection() {
        assertThrows(NullPointerException.class, () -> new EventBuilder(null));
    }

    @Test
    @DisplayName("Should reject null tags")
    void testNullTagRejection() {
        EventBuilder builder = new EventBuilder("Test");
        assertThrows(NullPointerException.class, () -> builder.addTag(null, "value"));
        assertThrows(NullPointerException.class, () -> builder.addTag("key", null));
    }

    @Test
    @DisplayName("Should handle NullPointerException")
    void testNullPointerException() {
        Throwable ex = new NullPointerException("Object reference is null");
        EventBuilder builder = new EventBuilder("NullPointerException occurred")
                .withException(ex)
                .addTag("type", "null_reference")
                .addTag("severity", "high");

        assertNotNull(builder.getException());
        assertEquals(NullPointerException.class, builder.getException().getClass());
        assertEquals("Object reference is null", builder.getException().getMessage());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void testIllegalArgumentException() {
        Throwable ex = new IllegalArgumentException("Invalid user ID provided");
        EventBuilder builder = new EventBuilder("Invalid argument")
                .withException(ex)
                .addTag("validation", "failed")
                .addExtra("user_input", "invalid-123");

        assertEquals(IllegalArgumentException.class, builder.getException().getClass());
        assertEquals("Invalid user ID provided", builder.getException().getMessage());
    }

    @Test
    @DisplayName("Should handle IOException")
    void testIOException() {
        java.io.IOException ex = new java.io.IOException("Failed to read configuration file");
        EventBuilder builder = new EventBuilder("IO Error")
                .withException(ex)
                .addTag("component", "config")
                .addTag("operation", "read")
                .addExtra("file_path", "/etc/bugtracker.conf");

        assertEquals(java.io.IOException.class, builder.getException().getClass());
    }

    @Test
    @DisplayName("Should handle nested exceptions")
    void testNestedExceptions() {
        Throwable rootCause = new java.sql.SQLException("Database connection failed");
        Throwable nested = new RuntimeException("Failed to execute query", rootCause);
        
        EventBuilder builder = new EventBuilder("Database operation failed")
                .withException(nested)
                .addTag("layer", "persistence")
                .addTag("database", "postgres")
                .addExtra("query_type", "SELECT")
                .addExtra("retry_count", 3);

        assertNotNull(builder.getException());
        assertEquals(RuntimeException.class, builder.getException().getClass());
        assertEquals(rootCause, builder.getException().getCause());
    }

    @Test
    @DisplayName("Should handle custom exception with context")
    void testCustomExceptionWithContext() {
        Throwable ex = new IllegalStateException("Payment processing failed");
        EventBuilder builder = new EventBuilder("Payment error")
                .withException(ex)
                .addTag("transaction_status", "failed")
                .addTag("payment_method", "credit_card")
                .addTag("retry_policy", "exponential_backoff")
                .addExtra("transaction_id", "txn_12345")
                .addExtra("amount", 99.99)
                .addExtra("currency", "USD")
                .addExtra("error_code", "INSUFFICIENT_FUNDS");

        assertEquals(IllegalStateException.class, builder.getException().getClass());
        assertEquals(3, builder.getTags().size());
        assertEquals(4, builder.getExtras().size());
    }

    @Test
    @DisplayName("Should handle exception with different severity levels")
    void testExceptionWithSeverityLevels() {
        EventBuilder criticalError = new EventBuilder("Critical system failure")
                .withException(new RuntimeException("System down"))
                .addTag("severity", "critical");
        
        EventBuilder warning = new EventBuilder("Degraded performance detected")
                .withException(new RuntimeException("High latency"))
                .addTag("severity", "warning");

        assertNotNull(criticalError.getException());
        assertNotNull(warning.getException());
    }
}
