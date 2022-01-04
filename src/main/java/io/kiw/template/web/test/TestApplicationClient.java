package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RoutesRegister;
import io.kiw.template.web.test.handler.BlockingTestHandler;
import io.kiw.template.web.test.handler.FailingTestHandler;
import io.kiw.template.web.test.handler.GetEchoHandler;
import io.kiw.template.web.test.handler.PostEchoHandler;

public class TestApplicationClient {

    private final StubRouter router = new StubRouter();

    public TestApplicationClient() {
        RoutesRegister routesRegister = new RoutesRegister(router);
        registerRoutes(routesRegister);

    }

    public static MyApplicationState registerRoutes(RoutesRegister routesRegister) {
        MyApplicationState myApplicationState = new MyApplicationState();

        routesRegister.registerJsonFilter("/root/*", myApplicationState, new TestFilter("rootFilter"));
        routesRegister.registerJsonFilter("/root/filter/*", myApplicationState, new TestFilter("pathFilter"));
        routesRegister.registerJsonFilter("/root/somethingElse/*", myApplicationState, new TestFilter("otherFilter"));
        routesRegister.registerJsonRoute("/root/filter/test", Method.POST, myApplicationState, new TestFilterHandler());

        routesRegister.registerJsonRoute("/echo", Method.POST, myApplicationState, new PostEchoHandler());
        routesRegister.registerJsonRoute("/echo", Method.PUT, myApplicationState, new PostEchoHandler());
        routesRegister.registerJsonRoute("/echo", Method.DELETE, myApplicationState, new PostEchoHandler());
        routesRegister.registerJsonRoute("/echo", Method.GET, myApplicationState, new GetEchoHandler());
        routesRegister.registerJsonRoute("/blocking", Method.POST, myApplicationState, new BlockingTestHandler());
        routesRegister.registerJsonRoute("/failing", Method.POST, myApplicationState, new FailingTestHandler());
        routesRegister.registerJsonRoute("/state", Method.POST, myApplicationState, new StateTestHandler());
        routesRegister.registerJsonRoute("/throw", Method.POST, myApplicationState, new ThrowTestHandler());
        return myApplicationState;
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
