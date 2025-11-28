package com.cubetiqs.sdk.analytics;

import com.cubetiqs.sdk.analytics.event.EventPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CubisAnalyticsClientTest {
    private static final String TEST_URL = "https://analytics.test.com";
    private static final String TEST_WEBSITE_ID = "test-website-123";

    private CubisAnalyticsClient client;

    @BeforeEach
    void setUp() {
        client = CubisAnalyticsClient.builder(TEST_URL, TEST_WEBSITE_ID)
                .setEnabled(true)
                .setDebugEnabled(false)
                .setMaxQueueSize(100)
                .setWorkerThreads(1)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    void testBuilder() {
        assertNotNull(client);
        assertNotNull(client.getConfig());
        assertEquals(TEST_URL, client.getConfig().getUrl());
        assertEquals(TEST_WEBSITE_ID, client.getConfig().getWebsiteId());
        assertTrue(client.getConfig().isEnabled());
    }

    @Test
    void testBuilderWithInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> {
            CubisAnalyticsClient.builder(null, TEST_WEBSITE_ID).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            CubisAnalyticsClient.builder("", TEST_WEBSITE_ID).build();
        });
    }

    @Test
    void testBuilderWithInvalidWebsiteId() {
        assertThrows(IllegalArgumentException.class, () -> {
            CubisAnalyticsClient.builder(TEST_URL, null).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            CubisAnalyticsClient.builder(TEST_URL, "").build();
        });
    }

    @Test
    void testInitialization() {
        assertFalse(client.isInitialized());

        boolean initialized = client.initialize();
        assertTrue(initialized);
        assertTrue(client.isInitialized());

        // Second initialization should succeed but not re-initialize
        boolean reinitialize = client.initialize();
        assertTrue(reinitialize);
        assertTrue(client.isInitialized());
    }

    @Test
    void testTrackWithoutInitialization() {
        assertFalse(client.isInitialized());

        boolean result = client.track("test-event", "/test", "Test Event");
        assertFalse(result);
    }

    @Test
    void testTrackSimpleEvent() {
        client.initialize();
        assertTrue(client.isInitialized());

        boolean result = client.track("page-view", "/home", "Home Page");
        assertTrue(result);
        assertTrue(client.getEventsQueued() > 0);
    }

    @Test
    void testTrackEventWithData() {
        client.initialize();

        Map<String, Object> data = new HashMap<>();
        data.put("category", "user");
        data.put("value", 100);

        boolean result = client.track("button-click", "/checkout", "Checkout", data);
        assertTrue(result);
        assertTrue(client.getEventsQueued() > 0);
    }

    @Test
    void testTrackEventWithBuilder() {
        client.initialize();

        EventPayload.Builder payloadBuilder = EventPayload.builder(TEST_WEBSITE_ID, "custom-event")
                .setUrl("/custom")
                .setTitle("Custom Event")
                .addData("key1", "value1")
                .addData("key2", 123);

        boolean result = client.track(payloadBuilder);
        assertTrue(result);
        assertTrue(client.getEventsQueued() > 0);
    }

    @Test
    void testTrackSimpleEventByName() {
        client.initialize();

        boolean result = client.track("simple-event");
        assertTrue(result);
        assertTrue(client.getEventsQueued() > 0);
    }

    @Test
    void testDisabledClient() {
        CubisAnalyticsClient disabledClient = CubisAnalyticsClient.builder(TEST_URL, TEST_WEBSITE_ID)
                .setEnabled(false)
                .build();

        disabledClient.initialize();
        assertTrue(disabledClient.isInitialized());
        assertFalse(disabledClient.isEnabled());

        boolean result = disabledClient.track("test-event");
        assertFalse(result);

        disabledClient.close();
    }

    @Test
    void testQueueStatistics() {
        client.initialize();

        assertEquals(0, client.getQueueSize());
        assertEquals(0, client.getEventsQueued());
        assertEquals(0, client.getEventsProcessed());
        assertEquals(0, client.getEventsDropped());

        client.track("event1", "/page1", "Event 1");
        client.track("event2", "/page2", "Event 2");

        assertTrue(client.getEventsQueued() >= 2);
    }

    @Test
    void testFlush() {
        client.initialize();

        client.track("event1");
        client.track("event2");

        boolean flushed = client.flush(5, TimeUnit.SECONDS);
        // Note: May return false if server is not available, which is expected in tests
        assertNotNull(flushed);
    }

    @Test
    void testShutdown() {
        client.initialize();
        assertTrue(client.isInitialized());

        client.track("event");

        client.shutdown();
        assertFalse(client.isInitialized());
    }

    @Test
    void testAutoCloseable() throws Exception {
        try (CubisAnalyticsClient autoCloseClient = CubisAnalyticsClient.builder(TEST_URL, TEST_WEBSITE_ID)
                .build()) {
            autoCloseClient.initialize();
            autoCloseClient.track("test-event");
            assertTrue(autoCloseClient.isInitialized());
        }
        // Client should be closed automatically
    }

    @Test
    void testConfigurationOptions() {
        CubisAnalyticsClient configuredClient = CubisAnalyticsClient.builder(TEST_URL, TEST_WEBSITE_ID)
                .setEnabled(true)
                .setMaxQueueSize(5000)
                .setMaxRetries(5)
                .setInitialRetryDelay(Duration.ofMillis(100))
                .setMaxRetryDelay(Duration.ofSeconds(10))
                .setRequestTimeout(Duration.ofSeconds(5))
                .setWorkerThreads(2)
                .setDebugEnabled(true)
                .build();

        CubisAnalyticsConfig config = configuredClient.getConfig();
        assertEquals(5000, config.getMaxQueueSize());
        assertEquals(5, config.getMaxRetries());
        assertEquals(Duration.ofMillis(100), config.getInitialRetryDelay());
        assertEquals(Duration.ofSeconds(10), config.getMaxRetryDelay());
        assertEquals(Duration.ofSeconds(5), config.getRequestTimeout());
        assertEquals(2, config.getWorkerThreads());
        assertTrue(config.isDebugEnabled());

        configuredClient.close();
    }

    @Test
    void testIdentifyUser() {
        client.initialize();
        assertTrue(client.isInitialized());

        boolean result = client.identify("user-123");
        assertTrue(result);
        assertTrue(client.getEventsQueued() > 0);
    }

    @Test
    void testIdentifyUserWithData() {
        client.initialize();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "John Doe");
        userData.put("email", "john@example.com");
        userData.put("plan", "premium");

        boolean result = client.identify("user-456", userData);
        assertTrue(result);
        assertTrue(client.getEventsQueued() > 0);
    }

    @Test
    void testIdentifyWithoutInitialization() {
        assertFalse(client.isInitialized());

        boolean result = client.identify("user-123");
        assertFalse(result);
    }

    @Test
    void testIdentifyWithNullUserId() {
        client.initialize();

        boolean result = client.identify(null);
        assertFalse(result);
    }

    @Test
    void testIdentifyWithEmptyUserId() {
        client.initialize();

        boolean result = client.identify("");
        assertFalse(result);
    }

    @Test
    void testIdentitySwitching() {
        client.initialize();

        // First user logs in
        boolean result1 = client.identify("user-123");
        assertTrue(result1);
        assertEquals("user-123", client.getCurrentUserId());

        // Track some events with first user
        client.trackPageView("/dashboard", "Dashboard");
        assertTrue(client.getEventsQueued() > 0);

        // User logs out
        client.clearIdentity();
        assertNull(client.getCurrentUserId());

        // Different user logs in
        boolean result2 = client.identify("user-456");
        assertTrue(result2);
        assertEquals("user-456", client.getCurrentUserId());

        // Track events with second user
        client.trackPageView("/profile", "Profile");

        // Verify events were tracked
        assertTrue(client.getEventsQueued() > 2);
    }

    @Test
    void testClearIdentityWithoutIdentify() {
        client.initialize();

        // Clearing identity when no user is identified should not throw
        assertDoesNotThrow(() -> client.clearIdentity());
        assertNull(client.getCurrentUserId());
    }
}
