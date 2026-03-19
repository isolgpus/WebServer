package io.kiw.web.test;

import io.kiw.web.infrastructure.Method;
import io.kiw.web.infrastructure.RoutesRegister;
import io.kiw.web.test.handler.*;
import io.kiw.web.test.handler.RouteConfigBuilder;
import io.kiw.web.test.jwt.StubJwtProvider;

public class TestApplicationRoutes {
    public static final String JWT_SECRET = "test-secret-key-for-unit-tests";

    public static MyApplicationState registerRoutes(RoutesRegister routesRegister, MyApplicationState state) {
        StubJwtProvider jwtProvider = new StubJwtProvider(JWT_SECRET);

        routesRegister.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
        routesRegister.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
        routesRegister.jsonFilter("/jwt/filter/*", state, new JwtFilter(jwtProvider));
        routesRegister.jsonFilter("/root/somethingElse/*", state, new TestFilter("otherFilter"));
        routesRegister.jsonRoute("/root/filter/test", Method.POST, state, new TestFilterHandler());
        routesRegister.jsonRoute("/root/filter/echo", Method.POST, state, new PostEchoHandler());
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
        routesRegister.webSocketRoute("/ws/blocking", state, new BlockingMapWebSocketHandler());
        routesRegister.webSocketRoute("/ws/asyncMap", state, new AsyncMapWebSocketHandler());
        routesRegister.webSocketRoute("/ws/asyncBlockingMap", state, new AsyncBlockingMapWebSocketHandler());
        routesRegister.webSocketRoute("/ws/flatMapFail", state, new FlatMapFailWebSocketHandler());
        routesRegister.webSocketRoute("/ws/blockingFlatMapFail", state, new BlockingFlatMapFailWebSocketHandler());
        routesRegister.webSocketRoute("/ws/asyncFlatMapFail", state, new AsyncFlatMapFailWebSocketHandler());
        routesRegister.webSocketRoute("/ws/throw", state, new ThrowWebSocketHandler());
        routesRegister.jsonRoute("/statusCode", Method.POST, state, new StatusCodeTestHandler());
        routesRegister.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(jwtProvider));
        routesRegister.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());

        routesRegister.jsonRoute("/openapi/echo", Method.POST, state, new PostEchoHandler(),
            new RouteConfigBuilder()
                .openApi()
                    .summary("Echo the input")
                    .description("Echoes back the provided values")
                    .tag("echo")
                    .responseDescription("The echoed response")
                .build()
        );
        routesRegister.jsonRoute("/openapi/echo/:pathExample", Method.GET, state, new GetEchoHandler(),
            new RouteConfigBuilder()
                .openApi()
                    .paramDescription("pathExample", "An example path param")
                .build()
        );
        routesRegister.jsonRoute("/openapi/hidden", Method.GET, state, new GetEchoHandler(),
            new RouteConfigBuilder()
                .openApi()
                    .hidden()
                .build()
        );
        routesRegister.jsonRoute("/openapi/timeout", Method.POST, state, new PostEchoHandler(),
            new RouteConfigBuilder()
                .timeout(5000)
                .openApi()
                    .summary("Echo with timeout")
                .done()
                .build()
        );
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", "A test API");

        return state;
    }
}
