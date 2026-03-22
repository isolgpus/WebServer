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

    @Override
    public void assertInExecutorContext() {
        final String threadName = Thread.currentThread().getName();
        if (!threadName.contains("Executor")) {
            throw new AssertionError("Expected to be on executor Context but was on: " + threadName);
        }
    }

    @Override
    public void evillySetExecutorModeInStub() {
        Thread.currentThread().setName("Executor");
    }
}
