package com.cubetiqs.sdk.bugtracker.transaction;

import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import java.util.Objects;

/**
 * Wrapper for managing transactions and performance monitoring.
 * Provides a simpler API for tracking distributed traces and performance metrics.
 */
public class TransactionManager implements AutoCloseable {
    private final ITransaction transaction;
    private final String name;
    private final String operation;

    public TransactionManager(String name, String operation) {
        this.name = Objects.requireNonNull(name, "name");
        this.operation = Objects.requireNonNull(operation, "operation");
        this.transaction = Sentry.startTransaction(name, operation);
    }

    public static TransactionManager start(String name, String operation) {
        return new TransactionManager(name, operation);
    }

    public ISpan startChild(String operation) {
        return startChild(operation, null);
    }

    public ISpan startChild(String operation, String description) {
        ISpan span = transaction.startChild(operation, description);
        return span;
    }

    public void finish() {
        if (transaction != null) {
            transaction.finish();
        }
    }

    @Override
    public void close() {
        finish();
    }

    public String getName() {
        return name;
    }

    public String getOperation() {
        return operation;
    }

    public ITransaction getTransaction() {
        return transaction;
    }
}
