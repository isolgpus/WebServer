package io.kiw.luxis.web;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.test.StubRouter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestLuxis<APP> implements Luxis<APP> {

    private final StubRouter router;
    private final APP applicationState;
    private final Consumer<Exception>[] exceptionHandlerRef;
    private final PendingAsyncResponses pendingAsyncResponses;

    @SuppressWarnings("unchecked")
    TestLuxis(final StubRouter router, final APP applicationState, final Consumer<Exception>[] exceptionHandlerRef, final PendingAsyncResponses pendingAsyncResponses) {
        this.router = router;
        this.applicationState = applicationState;
        this.exceptionHandlerRef = exceptionHandlerRef;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }



    public void setExceptionHandler(final Consumer<Exception> handler) {
        exceptionHandlerRef[0] = handler;
    }

    public StubRouter getRouter() {
        return router;
    }

    @Override
    public <IN> void apply(final IN immutableState, final BiConsumer<IN, APP> applicationStateConsumer) {
        applicationStateConsumer.accept(immutableState, applicationState);
    }

    @Override
    public <T> void handleAsyncResponse(final long correlationId, final Result<HttpErrorResponse, T> result) {
        pendingAsyncResponses.complete(correlationId, result);
    }

    @Override
    public void close() {
    }
}
