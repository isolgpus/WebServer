package io.kiw.web;

import io.kiw.web.infrastructure.RoutesRegister;
import io.kiw.web.test.StubRouter;
import io.kiw.web.test.WebSocketStubRouterWrapper;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestWebServer<APP> implements WebServer<APP> {

    private final StubRouter router;
    private final APP applicationState;
    private final Consumer<Exception>[] exceptionHandlerRef;

    @SuppressWarnings("unchecked")
    private TestWebServer(StubRouter router, APP applicationState, Consumer<Exception>[] exceptionHandlerRef) {
        this.router = router;
        this.applicationState = applicationState;
        this.exceptionHandlerRef = exceptionHandlerRef;
    }

    @SuppressWarnings("unchecked")
    public static <APP> TestWebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        Consumer<Exception>[] ref = new Consumer[]{e -> {}};
        StubRouter router = new StubRouter(e -> ref[0].accept(e));
        RoutesRegister routesRegister = new RoutesRegister(router, new WebSocketStubRouterWrapper());
        APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestWebServer<>(router, applicationState, ref);
    }

    @SuppressWarnings("unchecked")
    public static <APP> TestWebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer, WebServerConfig webServerConfig) {
        Consumer<Exception>[] ref = new Consumer[]{webServerConfig.exceptionHandler};
        StubRouter router = new StubRouter(e -> ref[0].accept(e));
        webServerConfig.corsConfig.ifPresent(router::configureCors);
        RoutesRegister routesRegister = new RoutesRegister(router, new WebSocketStubRouterWrapper());
        APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestWebServer<>(router, applicationState, ref);
    }

    public void setExceptionHandler(Consumer<Exception> handler) {
        exceptionHandlerRef[0] = handler;
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
