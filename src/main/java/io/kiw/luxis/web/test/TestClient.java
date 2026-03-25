package io.kiw.luxis.web.test;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;

public interface TestClient extends AutoCloseable {
    TestHttpResponse post(StubRequest stubRequest);

    TestHttpResponse put(StubRequest stubRequest);

    TestHttpResponse delete(StubRequest stubRequest);

    TestHttpResponse patch(StubRequest stubRequest);

    TestHttpResponse get(StubRequest stubRequest);

    TestHttpResponse options(StubRequest stubRequest);

    TestWebSocketClient webSocket(StubRequest stubRequest);

    void assertException(String expected);

    void assertNoMoreExceptions();

    <T> void handleAsyncResponse(long correlationId, Result<HttpErrorResponse, T> result);
}
