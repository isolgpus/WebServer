package io.kiw.luxis.web.test;

public class StubContextAsserter implements ContextAsserter {
    @Override
    public void assertInApplicationContext() {
        final String threadName = Thread.currentThread().getName();
        if (!threadName.contains("Application")) {
            throw new AssertionError("Expected to be on application context but was on: " + threadName);
        }
    }

    @Override
    public void assertInWorkerContext() {
        final String threadName = Thread.currentThread().getName();
        if (!threadName.contains("Worker")) {
            throw new AssertionError("Expected to be on Worker context but was on: " + threadName);
        }
    }
}
