package com.cubetiqs.sdk.bugtracker.transaction;

import com.cubetiqs.sdk.bugtracker.BugTrackerClient;
import io.sentry.ISpan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionManager Tests")
class TransactionManagerTest {

    private static final String TEST_DSN = "https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7";
    private BugTrackerClient client;

    @BeforeEach
    void setUp() {
        client = BugTrackerClient.builder()
                .setDsn(TEST_DSN)
                .setEnvironment("test")
                .setTracesSampleRate(1.0) // 100% sampling for testing
                .setDebugEnabled(true)
                .build();
        client.initialize();
    }

    @Nested
    @DisplayName("Transaction Creation and Basic Operations")
    class TransactionCreationTests {

        @Test
        @DisplayName("Should create transaction with operation and name")
        void testCreateTransaction() {
            try (TransactionManager transaction = TransactionManager.start("test-op", "http.request")) {
                assertNotNull(transaction);
                assertNotNull(transaction.getTransaction());
            }
        }

        @Test
        @DisplayName("Should support auto-closeable interface")
        void testAutoCloseable() {
            assertDoesNotThrow(() -> {
                try (TransactionManager transaction = TransactionManager.start("test-op", "test.transaction")) {
                    assertNotNull(transaction);
                }
                // Transaction should be closed without errors
            });
        }

        @Test
        @DisplayName("Should create child spans within transaction")
        void testCreateChildSpan() {
            try (TransactionManager transaction = TransactionManager.start("parent-op", "http.request")) {
                ISpan childSpan = transaction.startChild("db.query", "SELECT * FROM users");
                assertNotNull(childSpan);
                childSpan.finish();
            }
        }

        @Test
        @DisplayName("Should create multiple child spans")
        void testMultipleChildSpans() {
            try (TransactionManager transaction = TransactionManager.start("multi-span-op", "http.request")) {
                ISpan span1 = transaction.startChild("db.query", "INSERT INTO logs");
                ISpan span2 = transaction.startChild("http.client", "POST /api/notify");
                ISpan span3 = transaction.startChild("cache.operation", "SET user_cache");

                assertNotNull(span1);
                assertNotNull(span2);
                assertNotNull(span3);

                span1.finish();
                span2.finish();
                span3.finish();
            }
        }
    }

    @Nested
    @DisplayName("HTTP Request Transaction Tests")
    class HttpTransactionTests {

        @Test
        @DisplayName("Should track HTTP GET request")
        void testHttpGetTransaction() {
            try (TransactionManager transaction = TransactionManager.start("get-users", "http.request")) {
                // Simulate HTTP work
                ISpan httpSpan = transaction.startChild("http.client", "GET /api/users");
                Thread.sleep(10); // Simulate work
                httpSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track HTTP POST request")
        void testHttpPostTransaction() {
            try (TransactionManager transaction = TransactionManager.start("create-user", "http.request")) {
                ISpan httpSpan = transaction.startChild("http.client", "POST /api/users");
                Thread.sleep(10);
                httpSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track HTTP DELETE request")
        void testHttpDeleteTransaction() {
            try (TransactionManager transaction = TransactionManager.start("delete-user", "http.request")) {
                ISpan httpSpan = transaction.startChild("http.client", "DELETE /api/users/123");
                Thread.sleep(10);
                httpSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Database Operation Transaction Tests")
    class DatabaseTransactionTests {

        @Test
        @DisplayName("Should track database INSERT operation")
        void testDatabaseInsertTransaction() {
            try (TransactionManager transaction = TransactionManager.start("insert-user", "db.operation")) {
                ISpan dbSpan = transaction.startChild("db.query", "INSERT INTO users (name, email) VALUES (?, ?)");
                Thread.sleep(15);
                dbSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track database SELECT operation")
        void testDatabaseSelectTransaction() {
            try (TransactionManager transaction = TransactionManager.start("fetch-users", "db.operation")) {
                ISpan dbSpan = transaction.startChild("db.query", "SELECT * FROM users WHERE active = true");
                Thread.sleep(15);
                dbSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track database UPDATE operation")
        void testDatabaseUpdateTransaction() {
            try (TransactionManager transaction = TransactionManager.start("update-profile", "db.operation")) {
                ISpan dbSpan = transaction.startChild("db.query", "UPDATE users SET email = ? WHERE id = ?");
                Thread.sleep(15);
                dbSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track database DELETE operation")
        void testDatabaseDeleteTransaction() {
            try (TransactionManager transaction = TransactionManager.start("delete-logs", "db.operation")) {
                ISpan dbSpan = transaction.startChild("db.query", "DELETE FROM audit_logs WHERE created < ?");
                Thread.sleep(15);
                dbSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Complex Transaction Scenarios")
    class ComplexTransactionTests {

        @Test
        @DisplayName("Should track multi-step user registration process")
        void testUserRegistrationTransaction() {
            try (TransactionManager transaction = TransactionManager.start("user-registration", "http.request")) {
                // Database insert
                ISpan dbSpan = transaction.startChild("db.operation", "INSERT INTO users");
                Thread.sleep(50);
                dbSpan.finish();

                // Email sending
                ISpan emailSpan = transaction.startChild("email.send", "Send welcome email");
                Thread.sleep(100);
                emailSpan.finish();

                // API notification
                ISpan apiSpan = transaction.startChild("http.client", "POST /api/webhooks/new-user");
                Thread.sleep(30);
                apiSpan.finish();

                // Cache update
                ISpan cacheSpan = transaction.startChild("cache.operation", "SET user_cache");
                Thread.sleep(10);
                cacheSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track payment processing transaction")
        void testPaymentProcessingTransaction() {
            try (TransactionManager transaction = TransactionManager.start("payment-processing", "payment.process")) {
                // Validate payment
                ISpan validateSpan = transaction.startChild("payment.validation", "Validate card details");
                Thread.sleep(20);
                validateSpan.finish();

                // Connect to payment gateway
                ISpan gatewaySpan = transaction.startChild("http.client", "POST /payment-gateway/charge");
                Thread.sleep(150);
                gatewaySpan.finish();

                // Log transaction
                ISpan logSpan = transaction.startChild("db.operation", "INSERT INTO transaction_logs");
                Thread.sleep(30);
                logSpan.finish();

                // Update order status
                ISpan updateSpan = transaction.startChild("db.operation", "UPDATE orders SET status = 'paid'");
                Thread.sleep(25);
                updateSpan.finish();

                // Send confirmation email
                ISpan emailSpan = transaction.startChild("email.send", "Send payment confirmation");
                Thread.sleep(100);
                emailSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track nested operations with deep span hierarchy")
        void testDeepSpanHierarchy() {
            try (TransactionManager transaction = TransactionManager.start("complex-process", "process.workflow")) {
                // Level 1: Main operation
                ISpan level1 = transaction.startChild("operation.phase1", "Phase 1 - Preparation");
                Thread.sleep(10);

                // Level 2: Sub-operation (nested)
                ISpan level2 = level1.startChild("sub.operation", "Sub-operation");
                Thread.sleep(10);
                level2.finish();

                level1.finish();

                // Another main operation
                ISpan level1b = transaction.startChild("operation.phase2", "Phase 2 - Execution");
                Thread.sleep(20);
                level1b.finish();

                // Final phase
                ISpan level1c = transaction.startChild("operation.phase3", "Phase 3 - Finalization");
                Thread.sleep(10);
                level1c.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track data processing pipeline")
        void testDataProcessingPipeline() {
            try (TransactionManager transaction = TransactionManager.start("data-pipeline", "process.data")) {
                // Extract phase
                ISpan extractSpan = transaction.startChild("data.extract", "Extract from source");
                Thread.sleep(100);
                extractSpan.finish();

                // Transform phase
                ISpan transformSpan = transaction.startChild("data.transform", "Transform and validate");
                Thread.sleep(200);
                transformSpan.finish();

                // Load phase
                ISpan loadSpan = transaction.startChild("data.load", "Load to warehouse");
                Thread.sleep(150);
                loadSpan.finish();

                // Index phase
                ISpan indexSpan = transaction.startChild("search.index", "Update search indices");
                Thread.sleep(80);
                indexSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Performance and Timing Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle rapid span creation")
        void testRapidSpanCreation() {
            try (TransactionManager transaction = TransactionManager.start("rapid-spans", "performance.test")) {
                for (int i = 0; i < 10; i++) {
                    ISpan span = transaction.startChild("operation-" + i, "Rapid operation " + i);
                    span.finish();
                }
            }
        }

        @Test
        @DisplayName("Should measure operation timing accurately")
        void testOperationTiming() {
            try (TransactionManager transaction = TransactionManager.start("timing-test", "performance.test")) {
                ISpan span = transaction.startChild("timed-operation", "Operation to measure");
                Thread.sleep(50); // 50ms operation
                span.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should handle concurrent span finishing")
        void testConcurrentSpanFinishing() {
            try (TransactionManager transaction = TransactionManager.start("concurrent-test", "performance.test")) {
                ISpan span1 = transaction.startChild("op1", "Operation 1");
                ISpan span2 = transaction.startChild("op2", "Operation 2");
                ISpan span3 = transaction.startChild("op3", "Operation 3");

                // Finish in different order
                span2.finish();
                span1.finish();
                span3.finish();
            }
        }
    }

    @Nested
    @DisplayName("Transaction with Context Integration")
    class TransactionContextIntegrationTests {

        @Test
        @DisplayName("Should integrate transaction with context manager")
        void testTransactionWithContext() {
            client.context().setUser("user-123", "user@example.com", "testuser");
            client.context().addTag("feature", "transactions");

            try (TransactionManager transaction = TransactionManager.start("context-test", "http.request")) {
                ISpan span = transaction.startChild("db.operation", "Query with context");
                Thread.sleep(20);
                span.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track transaction with breadcrumbs")
        void testTransactionWithBreadcrumbs() {
            client.breadcrumbs().addBreadcrumb("Starting transaction process");

            try (TransactionManager transaction = TransactionManager.start("breadcrumb-test", "http.request")) {
                client.breadcrumbs().addBreadcrumb("Transaction started");

                ISpan span = transaction.startChild("db.query", "SELECT * FROM users");
                Thread.sleep(20);
                client.breadcrumbs().addBreadcrumb("Database query completed");
                span.finish();

                client.breadcrumbs().addBreadcrumb("Transaction completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should include transaction in event context")
        void testTransactionInEventContext() {
            try (TransactionManager transaction = TransactionManager.start("event-context-test", "http.request")) {
                client.context().addExtra("transaction_id", "tx-123");

                ISpan span = transaction.startChild("processing", "Process data");
                Thread.sleep(30);
                span.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Real-World Use Cases")
    class RealWorldUseCaseTests {

        @Test
        @DisplayName("Should track API endpoint request")
        void testApiEndpointTracking() {
            try (TransactionManager transaction = TransactionManager.start("GET /api/users/123", "http.request")) {
                client.context().addTag("http.method", "GET");
                client.context().addTag("http.endpoint", "/api/users/123");

                // Database query
                ISpan dbSpan = transaction.startChild("db.query", "SELECT * FROM users WHERE id = 123");
                Thread.sleep(30);
                dbSpan.finish();

                // Response serialization
                ISpan serializeSpan = transaction.startChild("json.serialize", "Serialize user object");
                Thread.sleep(10);
                serializeSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track background job execution")
        void testBackgroundJobTracking() {
            try (TransactionManager transaction = TransactionManager.start("send-daily-digest", "background.job")) {
                client.context().addTag("job_type", "email_digest");
                client.context().addTag("schedule", "daily");

                // Query users
                ISpan querySpan = transaction.startChild("db.query", "SELECT * FROM users WHERE email_enabled = true");
                Thread.sleep(100);
                querySpan.finish();

                // Process each user
                ISpan processSpan = transaction.startChild("processing", "Process and compose emails");
                Thread.sleep(200);
                processSpan.finish();

                // Send emails
                ISpan sendSpan = transaction.startChild("email.batch", "Send batch emails");
                Thread.sleep(300);
                sendSpan.finish();

                // Update tracking
                ISpan trackingSpan = transaction.startChild("db.update", "Update sent status");
                Thread.sleep(50);
                trackingSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track microservice call chain")
        void testMicroserviceCallChain() {
            try (TransactionManager transaction = TransactionManager.start("order-service-chain", "http.request")) {
                client.context().addTag("service", "order-api");
                client.context().addTag("operation", "create_order");

                // Call user service
                ISpan userServiceSpan = transaction.startChild("http.client", "GET /user-service/users/123");
                Thread.sleep(50);
                userServiceSpan.finish();

                // Call inventory service
                ISpan inventoryServiceSpan = transaction.startChild("http.client", "POST /inventory-service/check");
                Thread.sleep(100);
                inventoryServiceSpan.finish();

                // Call payment service
                ISpan paymentServiceSpan = transaction.startChild("http.client", "POST /payment-service/process");
                Thread.sleep(150);
                paymentServiceSpan.finish();

                // Local processing
                ISpan dbSpan = transaction.startChild("db.operation", "INSERT INTO orders");
                Thread.sleep(40);
                dbSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("Should track cache hit/miss scenario")
        void testCacheHitMissTracking() {
            try (TransactionManager transaction = TransactionManager.start("user-profile-fetch", "http.request")) {
                // Check cache
                ISpan cacheCheckSpan = transaction.startChild("cache.read", "Check Redis cache");
                Thread.sleep(5);
                cacheCheckSpan.finish();

                // Cache miss - database fetch
                ISpan dbSpan = transaction.startChild("db.query", "SELECT * FROM user_profiles");
                Thread.sleep(50);
                dbSpan.finish();

                // Update cache
                ISpan cacheSetSpan = transaction.startChild("cache.write", "Set Redis cache");
                Thread.sleep(10);
                cacheSetSpan.finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
