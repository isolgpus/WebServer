package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RoutesRegister;
import io.kiw.template.web.test.handler.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TestApplicationClient {

    private List<Exception> seenExceptions = new ArrayList<>();
    private final StubRouter router = new StubRouter(seenExceptions::add);

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
        routesRegister.jsonRoute("/blockingComplete", Method.POST, state, new BlockingCompleteTestHandler());
        routesRegister.jsonRoute("/timing", Method.POST, state, new TimeoutTestHandler());
        routesRegister.uploadFile("/upload", Method.POST, state, new FileUploaderHandler());
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

            if(exception.getMessage().equals(message))
            {
                iterator.remove();
                return;
            }
        }

        throw new AssertionError("Unable to find exception in seen exceptions " + seenExceptions.stream()
            .map(Throwable::getMessage).collect(Collectors.toList()));
    }
}
