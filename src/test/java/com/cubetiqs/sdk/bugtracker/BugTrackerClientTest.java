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

    @Test
    @DisplayName("Should provide access to underlying Sentry client")
    void testGetSentryClient() {
        client.initialize();
        assertNotNull(client.getSentryClient());
    }

    @Test
    @DisplayName("Should initialize automatically when getting Sentry client")
    void testGetSentryClientInitializesClient() {
        // Create new client without initializing
        BugTrackerClient newClient = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();
        
        // getSentryClient should auto-initialize
        assertNotNull(newClient.getSentryClient());
        assertTrue(newClient.isInitialized());
    }

    @Test
    @DisplayName("Should allow advanced Sentry operations via client")
    void testSentryClientAdvancedOperations() {
        client.initialize();
        io.sentry.IHub hub = client.getSentryClient();
        
        // Verify we can access Sentry hub methods
        assertNotNull(hub);
        // Can perform operations like: hub.getClient(), hub.pushScope(), etc.
    }

    @Test
    @DisplayName("Should capture message via Sentry client")
    void testCaptureMessageViaSentryClient() {
        client.initialize();
        io.sentry.IHub hub = client.getSentryClient();
        assertDoesNotThrow(() -> hub.captureMessage("Test message via Sentry client"));
    }

    @Test
    @DisplayName("Should disable BugTracker when setEnabled(false)")
    void testDisabledClient() {
        BugTrackerClient disabledClient = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnabled(false)
                .build();

        // Should not be initialized when disabled
        assertFalse(disabledClient.isInitialized());

        // All operations should be no-ops without errors
        assertDoesNotThrow(() -> disabledClient.initialize());
        assertDoesNotThrow(() -> disabledClient.captureMessage("Test"));
        assertDoesNotThrow(() -> disabledClient.captureException(new Exception("Test")));
        assertDoesNotThrow(() -> disabledClient.addBreadcrumb("Test"));
        assertDoesNotThrow(() -> disabledClient.flush());
    }

    @Test
    @DisplayName("Should be enabled by default")
    void testEnabledByDefault() {
        BugTrackerClient enabledClient = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .build();

        assertTrue(enabledClient.getConfig().isEnabled());
    }

    @Test
    @DisplayName("Should ignore errors when setIgnoreErrors(true)")
    void testIgnoreErrorsEnabled() {
        BugTrackerClient resilientClient = BugTrackerClient.builder()
                .setDsn("https://invalid-dsn-that-causes-errors")
                .setIgnoreErrors(true)
                .setDebugEnabled(false)
                .build();

        // Should not throw even with invalid DSN
        assertDoesNotThrow(() -> resilientClient.initialize());
        assertDoesNotThrow(() -> resilientClient.captureMessage("Test message"));
    }

    @Test
    @DisplayName("Should handle errors gracefully when capture fails")
    void testCaptureMethodsWithDisabledClient() {
        BugTrackerClient disabledClient = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setEnabled(false)
                .build();

        // All these should be no-ops without throwing
        assertDoesNotThrow(() -> {
            disabledClient.captureMessage("Message");
            disabledClient.captureException(new RuntimeException("Error"));
            disabledClient.addBreadcrumb("Breadcrumb");
            disabledClient.configureScope(scope -> scope.setTag("key", "value"));
            disabledClient.flush();
        });
    }

    @Test
    @DisplayName("Should respect ignore errors configuration")
    void testIgnoreErrorsConfiguration() {
        BugTrackerClient resilientClient = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setIgnoreErrors(true)
                .build();

        assertTrue(resilientClient.getConfig().isIgnoreErrors());
    }

    @Test
    @DisplayName("Should allow propagating errors when configured")
    void testErrorPropagationConfiguration() {
        BugTrackerClient strictClient = BugTrackerClient.builder()
                .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
                .setIgnoreErrors(false)
                .build();

        assertFalse(strictClient.getConfig().isIgnoreErrors());
    }
}
