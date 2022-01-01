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
        registerRoutes(routesRegister, new MyApplicationState());

    }

    public static void registerRoutes(RoutesRegister routesRegister, MyApplicationState myApplicationState) {
        routesRegister.registerJsonFilter("/root/*", new TestFilter("rootFilter"));
        routesRegister.registerJsonFilter("/root/filter/*", new TestFilter("pathFilter"));
        routesRegister.registerJsonFilter("/root/somethingElse/*", new TestFilter("otherFilter"));
        routesRegister.registerJsonRoute("/root/filter/test", Method.POST, new TestFilterHandler());

        routesRegister.registerJsonRoute("/echo", Method.POST, new PostEchoHandler());
        routesRegister.registerJsonRoute("/echo", Method.PUT, new PostEchoHandler());
        routesRegister.registerJsonRoute("/echo", Method.DELETE, new PostEchoHandler());
        routesRegister.registerJsonRoute("/echo", Method.GET, new GetEchoHandler());
        routesRegister.registerJsonRoute("/blocking", Method.POST, new BlockingTestHandler());
        routesRegister.registerJsonRoute("/failing", Method.POST, new FailingTestHandler());
        routesRegister.registerJsonRoute("/state", Method.POST, new StateTestHandler(myApplicationState));
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
