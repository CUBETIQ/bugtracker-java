package com.cubetiqs.sdk.analytics.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Payload for Umami analytics events.
 * Represents the data structure required by Umami's /api/send endpoint.
 */
public class EventPayload {
    private final String hostname;
    private final String screen;
    private final String language;
    private final String url;
    private final String referrer;
    private final String title;
    private final String tag;
    private final String id;
    private final String website;
    private final String name;
    private final Map<String, Object> data;

    private EventPayload(Builder builder) {
        this.hostname = builder.hostname;
        this.screen = builder.screen;
        this.language = builder.language;
        this.url = builder.url;
        this.referrer = builder.referrer;
        this.title = builder.title;
        this.tag = builder.tag;
        this.id = builder.id;
        this.website = builder.website;
        this.name = builder.name;
        this.data = builder.data != null ? new HashMap<>(builder.data) : null;
    }

    public String getHostname() {
        return hostname;
    }

    public String getScreen() {
        return screen;
    }

    public String getLanguage() {
        return language;
    }

    public String getUrl() {
        return url;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public String getId() {
        return id;
    }

    public String getWebsite() {
        return website;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Create a builder for custom events (with event name).
     * Use this for tracking custom events that will appear in the Events tab.
     */
    public static Builder builder(String website, String name) {
        return new Builder(website, name, false);
    }

    /**
     * Create a builder for pageview events (without event name).
     * Use this for tracking pageviews that will appear in the Overview/Statistics.
     */
    public static Builder pageviewBuilder(String website) {
        return new Builder(website, null, true);
    }

    public static class Builder {
        private final String website;
        private final String name;
        private String hostname;
        private String screen;
        private String language;
        private String url;
        private String referrer;
        private String title;
        private String tag;
        private String id;
        private Map<String, Object> data;

        private Builder(String website, String name, boolean isPageview) {
            if (website == null || website.trim().isEmpty()) {
                throw new IllegalArgumentException("Website ID is required");
            }
            // Name is required for custom events, but optional for pageviews
            if (!isPageview && (name == null || name.trim().isEmpty())) {
                throw new IllegalArgumentException("Event name is required");
            }
            this.website = website;
            this.name = name;
            this.id = UUID.randomUUID().toString();

            // Set defaults - Umami requires these fields
            this.hostname = getSystemHostname();
            this.language = getSystemLanguage();
            this.screen = "1920x1080";
            this.url = "/";
            this.referrer = "";
            this.title = name != null ? name : ""; // Default title to event name or empty for pageviews
        }

        public Builder setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder setScreen(String screen) {
            this.screen = screen;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setReferrer(String referrer) {
            this.referrer = referrer;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set custom data for the event.
         * This will be sent as the "data" field in the payload.
         */
        public Builder setData(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        /**
         * Add a single data entry to the event.
         */
        public Builder addData(String key, Object value) {
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.put(key, value);
            return this;
        }

        public EventPayload build() {
            return new EventPayload(this);
        }

        private static String getSystemHostname() {
            try {
                return java.net.InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                return "localhost";
            }
        }

        private static String getSystemLanguage() {
            try {
                String lang = System.getProperty("user.language", "en");
                String country = System.getProperty("user.country", "US");
                return lang + "-" + country;
            } catch (Exception e) {
                return "en-US";
            }
        }
    }
}
