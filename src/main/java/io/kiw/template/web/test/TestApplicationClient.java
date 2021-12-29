package io.kiw.template.web.test;

import io.kiw.template.web.test.handler.GetEchoHandler;
import io.kiw.template.web.test.handler.PostEchoHandler;
import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RoutesRegister;

public class TestApplicationClient {

    private final StubRouter router = new StubRouter();
    private final RoutesRegister routesRegister = new RoutesRegister(router);

    public TestApplicationClient() {
        routesRegister.registerJsonRoute("/echo", Method.POST, new PostEchoHandler());
        routesRegister.registerJsonRoute("/echo", Method.GET, new GetEchoHandler());

    }

    public StubHttpResponse post(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.POST);
    }

    public StubHttpResponse get(StubRequest stubRequest) {
        return router.handle(stubRequest, Method.GET);
    }
}
