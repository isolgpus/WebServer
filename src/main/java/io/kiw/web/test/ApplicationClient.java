package io.kiw.web.test;

public interface ApplicationClient {
    TestHttpResponse post(StubRequest stubRequest);

    TestHttpResponse put(StubRequest stubRequest);

    TestHttpResponse delete(StubRequest stubRequest);

    TestHttpResponse patch(StubRequest stubRequest);

    TestHttpResponse get(StubRequest stubRequest);

    TestHttpResponse options(StubRequest stubRequest);

    WebSocketClient webSocket(StubRequest stubRequest);

    void assertException(String expected);

    void assertNoMoreExceptions();
}
