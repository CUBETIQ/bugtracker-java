package com.cubetiqs.sdk.analytics.examples;

import com.cubetiqs.sdk.analytics.CubisAnalyticsClient;
import com.cubetiqs.sdk.analytics.event.EventPayload;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Examples demonstrating usage of CubisAnalyticsClient.
 */
public class AnalyticsExamples {
    private final static String ANALYTICS_URL = "https://analytics.ctdn.dev";
    private final static String WEBSITE_ID = "bc35afce-a498-4a67-afcf-9848d72270a5";

    public static void main(String[] args) {
        basicUsageExample();
        identifyUserExample();
        advancedConfigurationExample();
        customEventExample();
        ecommerceExample();
        webApplicationExample();
    }

    /**
     * Basic usage example with minimal configuration.
     */
    public static void basicUsageExample() {
        System.out.println("=== Basic Usage Example ===\n");

        // Create and initialize client
        CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .setEnabled(true)
                .setDebugEnabled(true)
                .build();

        analytics.initialize();

        // Track pageviews (will show in Umami Overview/Statistics)
        System.out.println("Tracking pageviews...");
        analytics.trackPageView("/", "Home Page");
        analytics.trackPageView("/about", "About Page");
        analytics.trackPageView("/contact", "Contact Page");

        // Track custom event (will show in Events tab)
        System.out.println("Tracking custom event...");
        Map<String, Object> data = new HashMap<>();
        data.put("category", "navigation");
        data.put("source", "menu");

        analytics.track("button-click", "/products", "View Products", data);

        // Cleanup
        analytics.flush(5, TimeUnit.SECONDS);
        analytics.close();

        System.out.println("\nBasic example complete\n");
    }

    /**
     * User identification example with login/logout scenarios.
     */
    public static void identifyUserExample() {
        System.out.println("=== User Identification Example (Login/Logout) ===\n");

        CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .setEnabled(true)
                .setDebugEnabled(true)
                .build();

        analytics.initialize();

        // User logs in - identify with user ID
        System.out.println("1. User logs in...");
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("name", "John Doe");
        user1Data.put("email", "john@example.com");
        user1Data.put("plan", "premium");

        analytics.identify("user-12345", user1Data);

        // All subsequent events will use user-12345 as the ID
        System.out.println("2. Tracking events with user-12345...");
        analytics.track("dashboard-view", "/dashboard", "Dashboard");
        analytics.track("profile-edit", "/profile", "Edit Profile");

        // User logs out - clear identity
        System.out.println("3. User logs out...");
        analytics.clearIdentity();

        // Events now use session ID
        System.out.println("4. Tracking events with session ID...");
        analytics.track("logout", "/", "Logged Out");

        // Different user logs in
        System.out.println("5. Different user logs in...");
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("name", "Jane Smith");
        user2Data.put("email", "jane@example.com");
        user2Data.put("plan", "basic");

        analytics.identify("user-67890", user2Data);

        // Events now use user-67890 as the ID
        System.out.println("6. Tracking events with user-67890...");
        analytics.track("dashboard-view", "/dashboard", "Dashboard");
        analytics.track("settings-view", "/settings", "Settings");

        analytics.flush(5, TimeUnit.SECONDS);
        analytics.close();

        System.out.println("\nUser identification example complete\n");
    }

    /**
     * Advanced configuration example with custom settings.
     */
    public static void advancedConfigurationExample() {
        System.out.println("=== Advanced Configuration Example ===\n");

        CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .setEnabled(true)
                .setMaxQueueSize(5000)              // Custom queue size
                .setMaxRetries(5)                    // More retry attempts
                .setInitialRetryDelay(Duration.ofMillis(100))
                .setMaxRetryDelay(Duration.ofSeconds(30))
                .setRequestTimeout(Duration.ofSeconds(10))
                .setWorkerThreads(2)                 // Multiple worker threads
                .setDebugEnabled(true)
                .build();

        analytics.initialize();

        // Track events
        for (int i = 0; i < 10; i++) {
            analytics.track("test-event-" + i, "/test", "Test Event " + i);
        }

        // Check statistics
        System.out.println("Queue size: " + analytics.getQueueSize());
        System.out.println("Events queued: " + analytics.getEventsQueued());
        System.out.println("Events processed: " + analytics.getEventsProcessed());

        analytics.close();

        System.out.println("\nAdvanced example complete\n");
    }

    /**
     * Custom event example using EventPayload builder.
     */
    public static void customEventExample() {
        System.out.println("=== Custom Event Example ===\n");

        CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .build();

        analytics.initialize();

        // Build custom event with all fields
        EventPayload.Builder payloadBuilder = EventPayload.builder("your-website-id", "custom-event")
                .setHostname("example.com")
                .setScreen("1920x1080")
                .setLanguage("en-US")
                .setUrl("/custom-page")
                .setReferrer("https://google.com")
                .setTitle("Custom Page Title")
                .setTag("important")
                .addData("user_id", "12345")
                .addData("plan", "premium")
                .addData("value", 99.99);

        analytics.track(payloadBuilder);

        analytics.close();

        System.out.println("Custom event example complete\n");
    }

    /**
     * E-commerce tracking example.
     */
    public static void ecommerceExample() {
        System.out.println("=== E-commerce Example ===\n");

        CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .setDebugEnabled(true)
                .build();

        analytics.initialize();

        // Product view
        Map<String, Object> productData = new HashMap<>();
        productData.put("product_id", "SKU-12345");
        productData.put("product_name", "Premium Widget");
        productData.put("price", 49.99);
        productData.put("currency", "USD");

        analytics.track("product-view", "/products/premium-widget", "Premium Widget", productData);

        // Add to cart
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("product_id", "SKU-12345");
        cartData.put("quantity", 2);
        cartData.put("total", 99.98);

        analytics.track("add-to-cart", "/cart", "Shopping Cart", cartData);

        // Checkout started
        Map<String, Object> checkoutData = new HashMap<>();
        checkoutData.put("cart_total", 99.98);
        checkoutData.put("items_count", 2);
        checkoutData.put("shipping_method", "express");

        analytics.track("checkout-started", "/checkout", "Checkout", checkoutData);

        // Purchase completed
        Map<String, Object> purchaseData = new HashMap<>();
        purchaseData.put("order_id", "ORD-67890");
        purchaseData.put("revenue", 109.97);  // Including shipping
        purchaseData.put("tax", 9.99);
        purchaseData.put("shipping", 9.99);
        purchaseData.put("items_count", 2);

        analytics.track("purchase", "/confirmation", "Order Confirmation", purchaseData);

        analytics.flush(5, TimeUnit.SECONDS);
        analytics.close();

        System.out.println("E-commerce example complete\n");
    }

    /**
     * Web application tracking example.
     */
    public static void webApplicationExample() {
        System.out.println("=== Web Application Example ===\n");

        CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .build();

        analytics.initialize();

        // User registration
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("source", "email_campaign");
        registrationData.put("plan", "free");

        analytics.track("user-registration", "/signup/complete", "Registration Complete", registrationData);

        // Feature usage
        Map<String, Object> featureData = new HashMap<>();
        featureData.put("feature_name", "export_data");
        featureData.put("duration_seconds", 45);

        analytics.track("feature-used", "/dashboard", "Dashboard", featureData);

        // Error tracking
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error_type", "ValidationError");
        errorData.put("error_message", "Invalid email format");
        errorData.put("field", "email");

        analytics.track("form-error", "/signup", "Sign Up Form", errorData);

        // User engagement
        Map<String, Object> engagementData = new HashMap<>();
        engagementData.put("session_duration", 1200);  // 20 minutes
        engagementData.put("pages_viewed", 15);
        engagementData.put("interactions", 42);

        analytics.track("session-end", "/", "Home", engagementData);

        analytics.close();

        System.out.println("Web application example complete\n");
    }

    /**
     * Try-with-resources example for automatic cleanup.
     */
    public static void tryWithResourcesExample() {
        System.out.println("=== Try-with-Resources Example ===\n");

        try (CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .build()) {

            analytics.initialize();

            // Track events
            analytics.track("event1", "/page1", "Page 1");
            analytics.track("event2", "/page2", "Page 2");

            // Client automatically closed at end of try block
        }

        System.out.println("Try-with-resources example complete\n");
    }

    /**
     * Background service example for long-running applications.
     */
    public static void backgroundServiceExample() {
        System.out.println("=== Background Service Example ===\n");

        CubisAnalyticsClient analytics = CubisAnalyticsClient.builder(
                        AnalyticsExamples.ANALYTICS_URL,
                        AnalyticsExamples.WEBSITE_ID
                )
                .setWorkerThreads(2)
                .setMaxQueueSize(10000)
                .build();

        analytics.initialize();

        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down analytics...");
            analytics.flush(10, TimeUnit.SECONDS);
            analytics.close();
        }));

        // Simulate background service
        for (int i = 0; i < 100; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("iteration", i);
            data.put("timestamp", System.currentTimeMillis());

            analytics.track("background-task", "/service", "Background Task", data);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("\nBackground service example complete\n");
    }
}
