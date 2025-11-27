package com.cubetiqs.sdk.analytics.internal;

import com.cubetiqs.sdk.analytics.event.AnalyticsEvent;
import com.cubetiqs.sdk.analytics.event.EventPayload;

import java.util.Map;

/**
 * Lightweight JSON serializer for analytics events.
 * Avoids external JSON library dependencies.
 */
public class JsonSerializer {

    /**
     * Serialize an AnalyticsEvent to JSON string.
     */
    public static String toJson(AnalyticsEvent event) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Add type
        json.append("\"type\":\"").append(escape(event.getType())).append("\",");

        // Add payload
        json.append("\"payload\":");
        json.append(payloadToJson(event.getPayload()));

        json.append("}");
        return json.toString();
    }

    /**
     * Serialize EventPayload to JSON string.
     */
    private static String payloadToJson(EventPayload payload) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        boolean first = true;

        // Add required fields
        if (payload.getHostname() != null) {
            json.append("\"hostname\":\"").append(escape(payload.getHostname())).append("\"");
            first = false;
        }

        if (payload.getScreen() != null) {
            if (!first) json.append(",");
            json.append("\"screen\":\"").append(escape(payload.getScreen())).append("\"");
            first = false;
        }

        if (payload.getLanguage() != null) {
            if (!first) json.append(",");
            json.append("\"language\":\"").append(escape(payload.getLanguage())).append("\"");
            first = false;
        }

        if (payload.getUrl() != null) {
            if (!first) json.append(",");
            json.append("\"url\":\"").append(escape(payload.getUrl())).append("\"");
            first = false;
        }

        if (payload.getReferrer() != null) {
            if (!first) json.append(",");
            json.append("\"referrer\":\"").append(escape(payload.getReferrer())).append("\"");
            first = false;
        }

        if (payload.getTitle() != null) {
            if (!first) json.append(",");
            json.append("\"title\":\"").append(escape(payload.getTitle())).append("\"");
            first = false;
        }

        if (payload.getTag() != null) {
            if (!first) json.append(",");
            json.append("\"tag\":\"").append(escape(payload.getTag())).append("\"");
            first = false;
        }

        if (payload.getId() != null) {
            if (!first) json.append(",");
            json.append("\"id\":\"").append(escape(payload.getId())).append("\"");
            first = false;
        }

        if (payload.getWebsite() != null) {
            if (!first) json.append(",");
            json.append("\"website\":\"").append(escape(payload.getWebsite())).append("\"");
            first = false;
        }

        if (payload.getName() != null) {
            if (!first) json.append(",");
            json.append("\"name\":\"").append(escape(payload.getName())).append("\"");
            first = false;
        }

        // Add data field if present
        if (payload.getData() != null && !payload.getData().isEmpty()) {
            if (!first) json.append(",");
            json.append("\"data\":");
            json.append(mapToJson(payload.getData()));
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Convert a map to JSON string.
     */
    private static String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escape(entry.getKey())).append("\":");
            json.append(valueToJson(entry.getValue()));
            first = false;
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Convert a value to JSON representation.
     */
    private static String valueToJson(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return mapToJson(map);
        }

        // Default: convert to string
        return "\"" + escape(value.toString()) + "\"";
    }

    /**
     * Escape special characters in JSON strings.
     */
    private static String escape(String str) {
        if (str == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < ' ' || c > '~') {
                        // Unicode escape for control characters
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }
}
