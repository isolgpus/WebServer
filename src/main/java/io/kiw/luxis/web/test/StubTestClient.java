package io.kiw.luxis.web.test;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.Method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class StubTestClient<APP> implements TestClient {

    private List<Exception> seenExceptions = new ArrayList<>();
    private final StubRouter router;
    private final Luxis<APP> luxis;


    public StubTestClient(final String host, final int port, final Luxis<APP> luxis) {
        this.luxis = luxis;
        final TestLuxis<APP> testWebServer = (TestLuxis<APP>) luxis;
        testWebServer.setExceptionHandler(seenExceptions::add);
        this.router = testWebServer.getRouter();
    }

    @Override
    public TestHttpResponse post(final StubRequest stubRequest) {

        return router.handle(stubRequest, Method.POST);
    }

    @Override
    public TestHttpResponse put(final StubRequest stubRequest) {

        return router.handle(stubRequest, Method.PUT);
    }

    @Override
    public TestHttpResponse delete(final StubRequest stubRequest) {

        return router.handle(stubRequest, Method.DELETE);
    }

    @Override
    public TestHttpResponse patch(final StubRequest stubRequest) {

        return router.handle(stubRequest, Method.PATCH);
    }

    @Override
    public TestHttpResponse get(final StubRequest stubRequest) {
        return router.handle(stubRequest, Method.GET);
    }

    @Override
    public TestHttpResponse options(final StubRequest stubRequest) {
        return router.handle(stubRequest, Method.OPTIONS);
    }

    @Override
    public StubTestWebSocketClient webSocket(final StubRequest stubRequest) {
        return router.webSocket(stubRequest);
    }

    public void assertNoMoreExceptions() {
        if (!this.seenExceptions.isEmpty()) {
            throw new AssertionError("Expected to find no exceptions but found " + seenExceptions.stream()
                .map(Throwable::getMessage).collect(Collectors.toList()));
        }
    }

    public void assertException(final String message) {
        final Iterator<Exception> iterator = this.seenExceptions.iterator();
        while (iterator.hasNext()) {
            final Exception exception = iterator.next();

            if (exception.getMessage().contains(message)) {
                iterator.remove();
                return;
            }
        }

        throw new AssertionError("Unable to find exception in seen exceptions " + seenExceptions.stream()
            .map(Throwable::getMessage).collect(Collectors.toList()));
    }

    @Override
    public <T> void handleAsyncResponse(final long correlationId, final Result<HttpErrorResponse, T> result) {
        luxis.handleAsyncResponse(correlationId, result);
    }

    @Override
    public void close() throws Exception {

    }
}
