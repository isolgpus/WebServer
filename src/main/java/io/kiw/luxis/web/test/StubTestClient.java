package io.kiw.luxis.web.test;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.http.Method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class StubTestClient implements TestClient {

    private List<Exception> seenExceptions = new ArrayList<>();
    private final StubRouter router;


    public StubTestClient(String host, int port, Luxis<MyApplicationState> luxis) {
        TestLuxis<MyApplicationState> testWebServer = (TestLuxis<MyApplicationState>) luxis;
        testWebServer.setExceptionHandler(seenExceptions::add);
        this.router = testWebServer.getRouter();
    }

    @Override
    public TestHttpResponse post(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.POST);
    }

    @Override
    public TestHttpResponse put(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.PUT);
    }

    @Override
    public TestHttpResponse delete(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.DELETE);
    }

    @Override
    public TestHttpResponse patch(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.PATCH);
    }

    @Override
    public TestHttpResponse get(StubRequest stubRequest) {
        return router.handle(stubRequest, Method.GET);
    }

    @Override
    public TestHttpResponse options(StubRequest stubRequest) {
        return router.handle(stubRequest, Method.OPTIONS);
    }

    @Override
    public StubTestWebSocketClient webSocket(StubRequest stubRequest) {
        return router.webSocket(stubRequest);
    }

    public void assertNoMoreExceptions(){
        if(!this.seenExceptions.isEmpty())
        {
            throw new AssertionError("Expected to find no exceptions but found " + seenExceptions.stream()
                .map(Throwable::getMessage).collect(Collectors.toList()));
        }
    }

    public void assertException(String message) {
        Iterator<Exception> iterator = this.seenExceptions.iterator();
        while(iterator.hasNext())
        {
            Exception exception = iterator.next();

            if(exception.getMessage().contains(message))
            {
                iterator.remove();
                return;
            }
        }

        throw new AssertionError("Unable to find exception in seen exceptions " + seenExceptions.stream()
            .map(Throwable::getMessage).collect(Collectors.toList()));
    }

    @Override
    public void close() throws Exception {

    }
}
