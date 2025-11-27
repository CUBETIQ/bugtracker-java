package com.cubetiqs.sdk.analytics.internal;

import com.cubetiqs.sdk.analytics.CubisAnalyticsConfig;
import com.cubetiqs.sdk.analytics.event.AnalyticsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Async event queue with worker threads for non-blocking event processing.
 * Provides high-performance, reliable event processing with bounded queue.
 */
public class EventQueue implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(EventQueue.class);

    private final CubisAnalyticsConfig config;
    private final EventSender sender;
    private final BlockingQueue<AnalyticsEvent> queue;
    private final ExecutorService executorService;
    private final AtomicBoolean running;
    private final AtomicLong eventsQueued;
    private final AtomicLong eventsProcessed;
    private final AtomicLong eventsDropped;

    public EventQueue(CubisAnalyticsConfig config, EventSender sender) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null");
        }

        this.config = config;
        this.sender = sender;
        this.queue = new LinkedBlockingQueue<>(config.getMaxQueueSize());
        this.running = new AtomicBoolean(true);
        this.eventsQueued = new AtomicLong(0);
        this.eventsProcessed = new AtomicLong(0);
        this.eventsDropped = new AtomicLong(0);

        // Create thread pool for worker threads
        this.executorService = Executors.newFixedThreadPool(
                config.getWorkerThreads(),
                new ThreadFactory() {
                    private final AtomicLong counter = new AtomicLong(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "CubisAnalytics-Worker-" + counter.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }
                }
        );

        // Start worker threads
        for (int i = 0; i < config.getWorkerThreads(); i++) {
            executorService.submit(new EventWorker());
        }

        logger.info("Event queue started with {} worker threads", config.getWorkerThreads());
    }

    /**
     * Add an event to the queue for async processing.
     * Non-blocking operation that returns immediately.
     *
     * @param event The event to queue
     * @return true if event was queued, false if queue is full or shutdown
     */
    public boolean enqueue(AnalyticsEvent event) {
        if (event == null) {
            logger.warn("Attempted to enqueue null event");
            return false;
        }

        if (!running.get()) {
            logger.warn("Queue is not running, dropping event: {}", event.getPayload().getName());
            eventsDropped.incrementAndGet();
            return false;
        }

        boolean added = queue.offer(event);
        if (added) {
            eventsQueued.incrementAndGet();
            logger.debug("Event queued: {} (queue size: {})", event.getPayload().getName(), queue.size());
        } else {
            eventsDropped.incrementAndGet();
            logger.warn("Queue full, dropping event: {}", event.getPayload().getName());
        }

        return added;
    }

    /**
     * Get the current queue size.
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Get total events queued since start.
     */
    public long getEventsQueued() {
        return eventsQueued.get();
    }

    /**
     * Get total events processed since start.
     */
    public long getEventsProcessed() {
        return eventsProcessed.get();
    }

    /**
     * Get total events dropped since start.
     */
    public long getEventsDropped() {
        return eventsDropped.get();
    }

    /**
     * Check if queue is running.
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Shutdown the queue and wait for pending events to be processed.
     *
     * @param timeout Maximum time to wait for shutdown
     * @return true if shutdown completed within timeout
     */
    public boolean shutdown(long timeout, TimeUnit unit) {
        logger.info("Shutting down event queue...");
        running.set(false);

        executorService.shutdown();
        try {
            boolean terminated = executorService.awaitTermination(timeout, unit);
            if (terminated) {
                logger.info("Event queue shutdown complete. Stats: queued={}, processed={}, dropped={}",
                        eventsQueued.get(), eventsProcessed.get(), eventsDropped.get());
            } else {
                logger.warn("Event queue shutdown timeout. Forcing shutdown...");
                executorService.shutdownNow();
            }
            return terminated;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            return false;
        }
    }

    @Override
    public void close() {
        shutdown(10, TimeUnit.SECONDS);
    }

    /**
     * Worker runnable that processes events from the queue.
     */
    private class EventWorker implements Runnable {
        @Override
        public void run() {
            logger.debug("Worker thread started: {}", Thread.currentThread().getName());

            while (running.get() || !queue.isEmpty()) {
                try {
                    // Poll with timeout to allow checking running flag
                    AnalyticsEvent event = queue.poll(1, TimeUnit.SECONDS);
                    if (event != null) {
                        processEvent(event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("Worker thread interrupted: {}", Thread.currentThread().getName());
                    break;
                } catch (Exception e) {
                    // Never let worker thread die due to exception
                    logger.error("Error in worker thread: {}", e.getMessage(), e);
                }
            }

            logger.debug("Worker thread stopped: {}", Thread.currentThread().getName());
        }

        private void processEvent(AnalyticsEvent event) {
            try {
                logger.debug("Processing event: {}", event.getPayload().getName());
                boolean success = sender.send(event);
                if (success) {
                    eventsProcessed.incrementAndGet();
                } else {
                    eventsDropped.incrementAndGet();
                }
            } catch (Exception e) {
                // Catch all exceptions to prevent worker thread from dying
                logger.error("Exception processing event: {}", e.getMessage(), e);
                eventsDropped.incrementAndGet();
            }
        }
    }
}
