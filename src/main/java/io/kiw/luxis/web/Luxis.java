package io.kiw.luxis.web;

import io.kiw.luxis.web.internal.RoutesRegister;
import io.kiw.luxis.web.internal.VertxRoutesRegistrar;
import io.kiw.luxis.web.test.StubRouter;
import io.kiw.luxis.web.test.WebSocketStubRouterWrapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Luxis<APP> extends AutoCloseable{


    public static <APP> Luxis<APP> start(final ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        return start(routesRegisterConsumer, new WebServiceConfigBuilder().build());
    }

    public static <APP> Luxis<APP> start(final ApplicationRoutesRegister<APP> routesRegisterConsumer, final WebServerConfig webServerConfig) {
        final Vertx vertx = Vertx.vertx();
        final HttpServer httpServer = vertx.createHttpServer();
        final Router router = Router.router(vertx);

        final APP applicationState = VertxRoutesRegistrar.register(router, vertx, routesRegisterConsumer, webServerConfig.defaultTimeoutMillis, webServerConfig.exceptionHandler, webServerConfig.maxBodySize, webServerConfig.corsConfig);

        httpServer.requestHandler(router).listen(webServerConfig.port).toCompletionStage().toCompletableFuture().join();
        return new VertxLuxis<>(vertx, applicationState);
    }


    @SuppressWarnings("unchecked")
    public static <APP> TestLuxis<APP> test(final ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        final Consumer<Exception>[] ref = new Consumer[]{e -> {}};
        final StubRouter router = new StubRouter(e -> ref[0].accept(e));
        final RoutesRegister routesRegister = new RoutesRegister(router, new WebSocketStubRouterWrapper());
        final APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestLuxis<>(router, applicationState, ref);
    }

    @SuppressWarnings("unchecked")
    public static <APP> TestLuxis<APP> test(final ApplicationRoutesRegister<APP> routesRegisterConsumer, final WebServerConfig webServerConfig) {
        final Consumer<Exception>[] ref = new Consumer[]{webServerConfig.exceptionHandler};
        final StubRouter router = new StubRouter(e -> ref[0].accept(e));
        webServerConfig.corsConfig.ifPresent(router::configureCors);
        final RoutesRegister routesRegister = new RoutesRegister(router, new WebSocketStubRouterWrapper());
        final APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestLuxis<>(router, applicationState, ref);
    }

    <IN> void apply(final IN immutableState, final BiConsumer<IN, APP> applicationStateConsumer);
}
