package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RoutesRegister;
import io.kiw.template.web.test.handler.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TestApplicationClient {

    public final List<Throwable> seenErrors = new ArrayList<>();
    private final StubRouter router = new StubRouter(seenErrors::add);

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
        routesRegister.registerJsonRoute("/blockingComplete", Method.POST, myApplicationState, new BlockingCompleteTestHandler());
        routesRegister.registerJsonRoute("/validateQueryParams", Method.GET, myApplicationState, new ValidateQueryParamsHandler());
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

    public void assertErrorSeen(String message) {
        Iterator<Throwable> seenErrorIterator = seenErrors.iterator();
        while(seenErrorIterator.hasNext())
        {
            Throwable seenError = seenErrorIterator.next();
            if(seenError.getMessage().contains(message))
            {
                seenErrorIterator.remove();
            }
            else
            {
                throw new AssertionError("Failed in find an exception containing error '" + message + "'\n" +
                        "Found " + seenErrors.stream().map(Throwable::getMessage).collect(Collectors.toList()));
            }
        }
    }

    public void assertNoMoreErrors() {
        if(seenErrors.size() > 0)
        {
            throw new AssertionError("Expected no more errors\n" +
                    "Found " + seenErrors.stream().map(Throwable::getMessage).collect(Collectors.toList()));
        }
    }
}
