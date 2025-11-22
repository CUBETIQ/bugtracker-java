package com.cubetiqs.sdk.bugtracker.context;

import io.sentry.protocol.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.cubetiqs.sdk.bugtracker.BugTrackerClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextManager Tests")
class ContextManagerTest {

    private ContextManager contextManager;

    @BeforeEach
    void setUp() {
        BugTrackerClient client = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setDebugEnabled(true)
                .build();
        contextManager = new ContextManager(client);
    }

    @Test
    @DisplayName("Should set user with builder")
    void testSetUser() {
        User user = new User();
        user.setId("user123");
        contextManager.setUser(user);
        assertEquals("user123", contextManager.getCurrentUser().getId());
    }

    @Test
    @DisplayName("Should add tags")
    void testAddTags() {
        contextManager.addTag("environment", "production");
        contextManager.addTag("service", "api");

        Map<String, String> tags = contextManager.getTags();
        assertEquals(2, tags.size());
    }

    @Test
    @DisplayName("Should add extras")
    void testAddExtras() {
        contextManager.addExtra("request_id", "abc123");
        contextManager.addExtra("user_count", 100);

        Map<String, Object> extras = contextManager.getExtras();
        assertEquals(2, extras.size());
    }

    @Test
    @DisplayName("Should clear context")
    void testClearContext() {
        contextManager.addTag("key", "value");
        contextManager.addExtra("key", "value");
        contextManager.clearContext();

        assertTrue(contextManager.getTags().isEmpty());
        assertTrue(contextManager.getExtras().isEmpty());
    }

    @Test
    @DisplayName("Should reject null values")
    void testNullRejection() {
        assertThrows(NullPointerException.class, () -> contextManager.addTag(null, "value"));
        assertThrows(NullPointerException.class, () -> contextManager.addTag("key", null));
    }
}
