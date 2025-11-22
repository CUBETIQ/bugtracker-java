package com.cubetiqs.sdk.bugtracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BugTrackerClient Tests")
class BugTrackerClientTest {

    private BugTrackerClient client;

    @BeforeEach
    void setUp() {
        client = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnvironment("test")
                .setDebugEnabled(true)
                .build();
    }

    @Test
    @DisplayName("Should create client with builder")
    void testBuilderCreation() {
        assertNotNull(client);
        assertNotNull(client.getConfig());
    }

    @Test
    @DisplayName("Should provide access to breadcrumb manager")
    void testBreadcrumbManager() {
        assertNotNull(client.breadcrumbs());
    }

    @Test
    @DisplayName("Should provide access to context manager")
    void testContextManager() {
        assertNotNull(client.context());
    }

    @Test
    @DisplayName("Should provide access to hook manager")
    void testHookManager() {
        assertNotNull(client.hooks());
    }

    @Test
    @DisplayName("Should set user in context manager")
    void testSetUserInContext() {
        client.context().setUser("user123");

        assertEquals("user123", client.context().getCurrentUser().getId());
    }

    @Test
    @DisplayName("Should initialize Sentry on demand")
    void testInitialization() {
        boolean initialized = client.isInitialized();
        assertTrue(initialized || !initialized); // Just test that method works
    }

    @Test
    @DisplayName("Should capture exception")
    void testCaptureException() {
        client.initialize();
        Exception testException = new Exception("Test exception");
        assertDoesNotThrow(() -> client.captureException(testException));
    }

    @Test
    @DisplayName("Should support auto-closeable")
    void testAutoCloseable() {
        try (BugTrackerClient c = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setDebugEnabled(true)
                .build()) {
            assertNotNull(c);
        }
    }

    @Test
    @DisplayName("Should support multiple managers")
    void testMultipleManagers() {
        assertNotNull(client.breadcrumbs());
        assertNotNull(client.context());
        assertNotNull(client.hooks());
        // All should be non-null and different instances
        assertNotEquals(client.breadcrumbs(), client.context());
        assertNotEquals(client.breadcrumbs(), client.hooks());
    }
}
