package com.cubetiqs.sdk.bugtracker.hook;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HookManager Tests")
class HookManagerTest {

    @Test
    @DisplayName("Should add and execute hooks")
    void testAddAndExecuteHooks() {
        HookManager manager = new HookManager();
        
        // Add a hook that transforms event
        manager.addHook((event, hint) -> {
            if (event != null) {
                event.setTag("transformed", "true");
            }
            return event;
        });

        SentryEvent event = new SentryEvent();
        
        SentryEvent result = manager.executeBeforeSend(event, new Hint());
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should chain multiple hooks")
    void testChainHooks() {
        HookManager manager = new HookManager();
        
        // First hook adds tag1
        manager.addHook((event, hint) -> {
            if (event != null) {
                event.setTag("hook", "first");
            }
            return event;
        });

        // Second hook adds tag2
        manager.addHook((event, hint) -> {
            if (event != null) {
                event.setTag("hook", "second");
            }
            return event;
        });

        assertEquals(2, manager.getHookCount());
    }

    @Test
    @DisplayName("Should allow dropping events")
    void testEventDropping() {
        HookManager manager = new HookManager();
        
        // Hook that drops events
        manager.addHook((event, hint) -> null);

        SentryEvent event = new SentryEvent();
        SentryEvent result = manager.executeBeforeSend(event, new Hint());
        assertNull(result);
    }

    @Test
    @DisplayName("Should clear hooks")
    void testClearHooks() {
        HookManager manager = new HookManager();
        manager.addHook((event, hint) -> event);
        manager.addHook((event, hint) -> event);
        
        assertEquals(2, manager.getHookCount());
        
        manager.clear();
        assertEquals(0, manager.getHookCount());
    }
}
