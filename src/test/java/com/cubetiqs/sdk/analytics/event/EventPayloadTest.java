package com.cubetiqs.sdk.analytics.event;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventPayloadTest {

    private static final String TEST_WEBSITE_ID = "test-website-123";
    private static final String TEST_EVENT_NAME = "test-event";

    @Test
    void testBuilderWithRequiredFields() {
        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME).build();

        assertNotNull(payload);
        assertEquals(TEST_WEBSITE_ID, payload.getWebsite());
        assertEquals(TEST_EVENT_NAME, payload.getName());
        assertNotNull(payload.getId());
        assertNotNull(payload.getHostname());
        assertNotNull(payload.getLanguage());
        assertNotNull(payload.getScreen());
    }

    @Test
    void testBuilderWithAllFields() {
        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", 123);

        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .setHostname("test-host")
                .setScreen("1920x1080")
                .setLanguage("en-US")
                .setUrl("/test-page")
                .setReferrer("https://example.com")
                .setTitle("Test Page")
                .setTag("test-tag")
                .setId("custom-id-123")
                .setData(data)
                .build();

        assertNotNull(payload);
        assertEquals("test-host", payload.getHostname());
        assertEquals("1920x1080", payload.getScreen());
        assertEquals("en-US", payload.getLanguage());
        assertEquals("/test-page", payload.getUrl());
        assertEquals("https://example.com", payload.getReferrer());
        assertEquals("Test Page", payload.getTitle());
        assertEquals("test-tag", payload.getTag());
        assertEquals("custom-id-123", payload.getId());
        assertNotNull(payload.getData());
        assertEquals("value1", payload.getData().get("key1"));
        assertEquals(123, payload.getData().get("key2"));
    }

    @Test
    void testBuilderWithDataEntries() {
        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .addData("foo", "bar")
                .addData("count", 42)
                .addData("enabled", true)
                .build();

        assertNotNull(payload.getData());
        assertEquals(3, payload.getData().size());
        assertEquals("bar", payload.getData().get("foo"));
        assertEquals(42, payload.getData().get("count"));
        assertEquals(true, payload.getData().get("enabled"));
    }

    @Test
    void testBuilderWithNullWebsiteId() {
        assertThrows(IllegalArgumentException.class, () -> {
            EventPayload.builder(null, TEST_EVENT_NAME).build();
        });
    }

    @Test
    void testBuilderWithEmptyWebsiteId() {
        assertThrows(IllegalArgumentException.class, () -> {
            EventPayload.builder("", TEST_EVENT_NAME).build();
        });
    }

    @Test
    void testBuilderWithNullEventName() {
        assertThrows(IllegalArgumentException.class, () -> {
            EventPayload.builder(TEST_WEBSITE_ID, null).build();
        });
    }

    @Test
    void testBuilderWithEmptyEventName() {
        assertThrows(IllegalArgumentException.class, () -> {
            EventPayload.builder(TEST_WEBSITE_ID, "").build();
        });
    }

    @Test
    void testDefaultValues() {
        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME).build();

        assertNotNull(payload.getHostname());
        assertNotNull(payload.getLanguage());
        assertEquals("1920x1080", payload.getScreen());
        assertEquals("/", payload.getUrl());
        assertEquals("", payload.getReferrer());
    }

    @Test
    void testIdGeneration() {
        EventPayload payload1 = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME).build();
        EventPayload payload2 = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME).build();

        assertNotNull(payload1.getId());
        assertNotNull(payload2.getId());
        assertNotEquals(payload1.getId(), payload2.getId());
    }

    @Test
    void testDataImmutability() {
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("key", "value");

        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .setData(originalData)
                .build();

        // Modify original data
        originalData.put("key", "modified");
        originalData.put("new-key", "new-value");

        // Payload data should not be affected
        assertEquals("value", payload.getData().get("key"));
        assertNull(payload.getData().get("new-key"));
    }

    @Test
    void testPageviewBuilderWithoutName() {
        // Pageview events should not have a name field
        EventPayload payload = EventPayload.pageviewBuilder(TEST_WEBSITE_ID)
                .setUrl("/page")
                .setTitle("Page Title")
                .build();

        assertNotNull(payload);
        assertEquals(TEST_WEBSITE_ID, payload.getWebsite());
        assertNull(payload.getName()); // Name should be null for pageviews
        assertEquals("/page", payload.getUrl());
        assertEquals("Page Title", payload.getTitle());
    }

    @Test
    void testPageviewBuilderDefaults() {
        EventPayload payload = EventPayload.pageviewBuilder(TEST_WEBSITE_ID).build();

        assertNotNull(payload);
        assertNull(payload.getName()); // Pageviews don't have names
        assertNotNull(payload.getHostname());
        assertNotNull(payload.getLanguage());
        assertEquals("1920x1080", payload.getScreen());
        assertEquals("/", payload.getUrl());
        assertEquals("", payload.getReferrer());
        assertEquals("", payload.getTitle()); // Empty title for pageviews when not set
    }
}
