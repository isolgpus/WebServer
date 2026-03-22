package io.kiw.luxis.web.test;

import io.kiw.luxis.web.internal.ExecutionDispatcher;

public class StubExecutionDispatcher implements ExecutionDispatcher {
    @Override
    public void handleBlocking(final Runnable o) {
        o.run();
    }

    @Override
    public void handleOnEventLoop(final Runnable o) {
        o.run();
    }
}
