package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RoutesRegister;
import io.kiw.template.web.test.handler.*;

public class TestApplicationClient {

    private final StubRouter router = new StubRouter();

    public TestApplicationClient() {
        RoutesRegister routesRegister = new RoutesRegister(router);
        registerRoutes(routesRegister);

    }

    public static MyApplicationState registerRoutes(RoutesRegister routesRegister) {
        MyApplicationState state = new MyApplicationState();

        routesRegister.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
        routesRegister.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
        routesRegister.jsonFilter("/root/somethingElse/*", state, new TestFilter("otherFilter"));
        routesRegister.jsonRoute("/root/filter/test", Method.POST, state, new TestFilterHandler());

        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.PUT, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.DELETE, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        routesRegister.jsonRoute("/blocking", Method.POST, state, new BlockingTestHandler());
        routesRegister.jsonRoute("/failing", Method.POST, state, new FailingTestHandler());
        routesRegister.jsonRoute("/state", Method.POST, state, new StateTestHandler());
        routesRegister.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        routesRegister.jsonRoute("/timeout", Method.POST, state, new TimeoutTestHandler(),
            new RouteConfigBuilder().timeout(200).build());
        return state;
    }

    public StubHttpResponse post(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.POST);
    }

    public StubHttpResponse put(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.PUT);
    }

    public StubHttpResponse delete(StubRequest stubRequest) {

        return router.handle(stubRequest, Method.DELETE);
    }

    public StubHttpResponse get(StubRequest stubRequest) {
        return router.handle(stubRequest, Method.GET);
    }
}
