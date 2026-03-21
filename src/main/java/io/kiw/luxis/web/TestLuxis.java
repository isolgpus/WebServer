package io.kiw.luxis.web;

import io.kiw.luxis.web.test.StubRouter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestLuxis<APP> implements Luxis<APP> {

    private final StubRouter router;
    private final APP applicationState;
    private final Consumer<Exception>[] exceptionHandlerRef;

    @SuppressWarnings("unchecked")
    TestLuxis(StubRouter router, APP applicationState, Consumer<Exception>[] exceptionHandlerRef) {
        this.router = router;
        this.applicationState = applicationState;
        this.exceptionHandlerRef = exceptionHandlerRef;
    }



    public void setExceptionHandler(Consumer<Exception> handler) {
        exceptionHandlerRef[0] = handler;
    }

    public StubRouter getRouter() {
        return router;
    }

    @Override
    public <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer) {
        applicationStateConsumer.accept(immutableState, applicationState);
    }

    @Override
    public void close() {
    }
}
