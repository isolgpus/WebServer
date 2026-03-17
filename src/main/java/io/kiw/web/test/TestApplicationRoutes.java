package io.kiw.web.test;

import io.kiw.web.infrastructure.Method;
import io.kiw.web.infrastructure.RoutesRegister;
import io.kiw.web.test.handler.*;

public class TestApplicationRoutes {
    public static MyApplicationState registerRoutes(RoutesRegister routesRegister, MyApplicationState state) {

        routesRegister.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
        routesRegister.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
        routesRegister.jsonFilter("/root/somethingElse/*", state, new TestFilter("otherFilter"));
        routesRegister.jsonRoute("/root/filter/test", Method.POST, state, new TestFilterHandler());
        routesRegister.jsonRoute("/root/filter/test", Method.GET, state, new GetTestFilterHandler());
        routesRegister.jsonRoute("/root/filter/test", Method.PUT, state, new TestFilterHandler());
        routesRegister.jsonRoute("/root/filter/test", Method.DELETE, state, new TestFilterHandler());
        routesRegister.jsonRoute("/root/filter/test", Method.PATCH, state, new TestFilterHandler());

        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.PUT, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.DELETE, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.PATCH, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        routesRegister.jsonRoute("/echo/:pathExample", Method.GET, state, new GetEchoHandler());
        routesRegister.jsonRoute("/blocking", Method.POST, state, new BlockingTestHandler());
        routesRegister.jsonRoute("/failing", Method.POST, state, new FailingTestHandler());
        routesRegister.jsonRoute("/state", Method.POST, state, new StateTestHandler());
        routesRegister.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        routesRegister.jsonRoute("/blockingComplete", Method.POST, state, new BlockingCompleteTestHandler());
        routesRegister.jsonRoute("/timing", Method.POST, state, new TimeoutTestHandler());
        routesRegister.jsonRoute("/asyncMap", Method.POST, state, new AsyncMapTestHandler());
        routesRegister.jsonRoute("/asyncBlockingMap", Method.POST, state, new AsyncBlockingMapTestHandler());
        routesRegister.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        routesRegister.jsonFilter("/protected/*", state, new ErrorFilter());
        routesRegister.jsonRoute("/protected/resource", Method.GET, state, new GetEchoHandler());
        routesRegister.jsonRoute("/blockingFailing", Method.POST, state, new BlockingFlatMapFailHandler());
        routesRegister.jsonRoute("/asyncFailing", Method.POST, state, new AsyncFlatMapFailHandler());
        routesRegister.uploadFileRoute("/upload", Method.POST, state, new FileUploaderHandler());
        routesRegister.downloadFileRoute("/download", Method.GET, state, new FileDownloaderHandler(), "text/html; charset=utf-8");
        routesRegister.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        routesRegister.webSocketRoute("/ws/chat/:room", state, new StatefulWebSocketHandler());
        routesRegister.jsonRoute("/statusCode", Method.POST, state, new StatusCodeTestHandler());
        return state;
    }
}
