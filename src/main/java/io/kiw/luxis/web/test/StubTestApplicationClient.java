package io.kiw.luxis.web.test;

import io.kiw.luxis.web.TestWebServer;
import io.kiw.luxis.web.WebServer;
import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.internal.RoutesRegister;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StubTestApplicationClient implements TestApplicationClient {

    private List<Exception> seenExceptions = new ArrayList<>();
    private final StubRouter router;

    public StubTestApplicationClient(final Consumer<RoutesRegister> registerRoutes) {
        this(registerRoutes, null);
    }

    public StubTestApplicationClient(final Consumer<RoutesRegister> registerRoutes, CorsConfig corsConfig) {
        this.router = new StubRouter(seenExceptions::add);
        if(corsConfig != null) {
            router.configureCors(corsConfig);
        }
        RoutesRegister routesRegister = new RoutesRegister(router, new WebSocketStubRouterWrapper());
        registerRoutes.accept(routesRegister);
    }

    public StubTestApplicationClient(String host, int port, WebServer<MyApplicationState> webServer) {
        TestWebServer<MyApplicationState> testWebServer = (TestWebServer<MyApplicationState>) webServer;
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

    @Override
    public void stop() {

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
}
