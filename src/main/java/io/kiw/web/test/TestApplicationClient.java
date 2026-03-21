package io.kiw.web.test;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;
import io.kiw.web.openapi.*;

public interface TestApplicationClient {
    TestHttpResponse post(StubRequest stubRequest);

    TestHttpResponse put(StubRequest stubRequest);

    TestHttpResponse delete(StubRequest stubRequest);

    TestHttpResponse patch(StubRequest stubRequest);

    TestHttpResponse get(StubRequest stubRequest);

    TestHttpResponse options(StubRequest stubRequest);

    TestWebSocketClient webSocket(StubRequest stubRequest);

    void assertException(String expected);

    void assertNoMoreExceptions();

    void stop();
}
