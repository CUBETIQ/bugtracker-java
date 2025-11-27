package com.cubetiqs.sdk.analytics.internal;

import com.cubetiqs.sdk.analytics.event.AnalyticsEvent;
import com.cubetiqs.sdk.analytics.event.EventPayload;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonSerializerTest {

    private static final String TEST_WEBSITE_ID = "test-website-123";
    private static final String TEST_EVENT_NAME = "test-event";

    @Test
    void testSerializeBasicEvent() {
        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .setUrl("/test")
                .setTitle("Test Title")
                .build();

        AnalyticsEvent event = new AnalyticsEvent(payload);
        String json = JsonSerializer.toJson(event);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"event\""));
        assertTrue(json.contains("\"payload\""));
        assertTrue(json.contains("\"website\":\"" + TEST_WEBSITE_ID + "\""));
        assertTrue(json.contains("\"name\":\"" + TEST_EVENT_NAME + "\""));
        assertTrue(json.contains("\"url\":\"/test\""));
        assertTrue(json.contains("\"title\":\"Test Title\""));
    }

    @Test
    void testSerializeEventWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("stringValue", "hello");
        data.put("numberValue", 123);
        data.put("booleanValue", true);

        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .setUrl("/test")
                .setData(data)
                .build();

        AnalyticsEvent event = new AnalyticsEvent(payload);
        String json = JsonSerializer.toJson(event);

        assertNotNull(json);
        assertTrue(json.contains("\"data\""));
        assertTrue(json.contains("\"stringValue\":\"hello\""));
        assertTrue(json.contains("\"numberValue\":123"));
        assertTrue(json.contains("\"booleanValue\":true"));
    }

    @Test
    void testSerializeEventWithSpecialCharacters() {
        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .setTitle("Test \"Title\" with 'quotes'")
                .setUrl("/test?param=value&other=123")
                .addData("message", "Line 1\nLine 2\tTabbed")
                .build();

        AnalyticsEvent event = new AnalyticsEvent(payload);
        String json = JsonSerializer.toJson(event);

        assertNotNull(json);
        assertTrue(json.contains("\\\""));  // Escaped quotes
        assertTrue(json.contains("\\n"));   // Escaped newline
        assertTrue(json.contains("\\t"));   // Escaped tab
    }

    @Test
    void testSerializeEventWithNullValues() {
        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .setUrl("/test")
                .setReferrer(null)
                .setTag(null)
                .build();

        AnalyticsEvent event = new AnalyticsEvent(payload);
        String json = JsonSerializer.toJson(event);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"event\""));
    }

    @Test
    void testSerializeCompleteEvent() {
        Map<String, Object> data = new HashMap<>();
        data.put("category", "user");
        data.put("action", "click");
        data.put("value", 100);

        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME)
                .setHostname("example.com")
                .setScreen("1920x1080")
                .setLanguage("en-US")
                .setUrl("/checkout")
                .setReferrer("https://google.com")
                .setTitle("Checkout Page")
                .setTag("conversion")
                .setId("session-123")
                .setData(data)
                .build();

        AnalyticsEvent event = new AnalyticsEvent(payload);
        String json = JsonSerializer.toJson(event);

        assertNotNull(json);

        // Verify all fields are present
        assertTrue(json.contains("\"type\":\"event\""));
        assertTrue(json.contains("\"hostname\":\"example.com\""));
        assertTrue(json.contains("\"screen\":\"1920x1080\""));
        assertTrue(json.contains("\"language\":\"en-US\""));
        assertTrue(json.contains("\"url\":\"/checkout\""));
        assertTrue(json.contains("\"referrer\":\"https://google.com\""));
        assertTrue(json.contains("\"title\":\"Checkout Page\""));
        assertTrue(json.contains("\"tag\":\"conversion\""));
        assertTrue(json.contains("\"id\":\"session-123\""));
        assertTrue(json.contains("\"website\":\"" + TEST_WEBSITE_ID + "\""));
        assertTrue(json.contains("\"name\":\"" + TEST_EVENT_NAME + "\""));
        assertTrue(json.contains("\"data\""));
        assertTrue(json.contains("\"category\":\"user\""));
        assertTrue(json.contains("\"action\":\"click\""));
        assertTrue(json.contains("\"value\":100"));
    }

    @Test
    void testJsonStructure() {
        EventPayload payload = EventPayload.builder(TEST_WEBSITE_ID, TEST_EVENT_NAME).build();
        AnalyticsEvent event = new AnalyticsEvent(payload);
        String json = JsonSerializer.toJson(event);

        // Verify JSON structure
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"payload\":{"));
    }
}
