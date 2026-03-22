package io.kiw.luxis.web.test;

public class VertxContextAsserter implements ContextAsserter {
    @Override
    public void assertInApplicationContext() {
        final String threadName = Thread.currentThread().getName();
        if (!threadName.contains("vert.x-eventloop-thread")) {
            throw new AssertionError("Expected to be on vert.x-eventloop-thread but was on: " + threadName);
        }
    }

    @Override
    public void assertInWorkerContext() {
        final String threadName = Thread.currentThread().getName();
        if (!threadName.contains("vert.x-worker-thread")) {
            throw new AssertionError("Expected to be on vert.x-worker-thread but was on: " + threadName);
        }
    }

}
