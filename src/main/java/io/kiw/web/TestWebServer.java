package io.kiw.web;

import io.kiw.web.infrastructure.RoutesRegister;
import io.kiw.web.test.StubRouter;
import io.kiw.web.test.WebSocketStubRouterWrapper;

import java.util.function.BiConsumer;

public class TestWebServer<APP> implements WebServer<APP> {

    private final StubRouter router;
    private final APP applicationState;

    private TestWebServer(StubRouter router, APP applicationState) {
        this.router = router;
        this.applicationState = applicationState;
    }

    public static <APP> WebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        StubRouter router = new StubRouter(e -> {});
        RoutesRegister routesRegister = new RoutesRegister(router, new WebSocketStubRouterWrapper());
        APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestWebServer<>(router, applicationState);
    }

    public static <APP> WebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer, WebServerConfig webServerConfig) {
        StubRouter router = new StubRouter(webServerConfig.exceptionHandler);
        webServerConfig.corsConfig.ifPresent(router::configureCors);
        RoutesRegister routesRegister = new RoutesRegister(router, new WebSocketStubRouterWrapper());
        APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestWebServer<>(router, applicationState);
    }

    public StubRouter getRouter() {
        return router;
    }

    @Override
    public <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer) {
        applicationStateConsumer.accept(immutableState, applicationState);
    }

    @Override
    public void stop() {
    }
}
