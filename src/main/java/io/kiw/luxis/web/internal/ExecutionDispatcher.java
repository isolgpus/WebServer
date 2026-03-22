package io.kiw.luxis.web.internal;

public interface ExecutionDispatcher {
    void handleBlocking(Runnable o);

    void handleOnEventLoop(Runnable o);
}
