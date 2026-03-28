package io.kiw.luxis.web;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RoutesRegister;
import io.kiw.luxis.web.internal.VertxExecutionDispatcher;
import io.kiw.luxis.web.internal.VertxRoutesRegistrar;
import io.kiw.luxis.web.internal.VertxTimeoutScheduler;
import io.kiw.luxis.web.test.StubRouter;
import io.kiw.luxis.web.test.StubExecutionDispatcher;
import io.kiw.luxis.web.test.StubTimeoutScheduler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Luxis<APP> extends AutoCloseable {


    static <APP> Luxis<APP> start(final ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        return start(routesRegisterConsumer, new WebServiceConfigBuilder().build());
    }

    static <APP> Luxis<APP> start(final ApplicationRoutesRegister<APP> routesRegisterConsumer, final WebServerConfig webServerConfig) {
        final Vertx vertx = Vertx.vertx();
        final HttpServer httpServer = vertx.createHttpServer();
        final Router router = Router.router(vertx);

        final VertxExecutionDispatcher executionDispatcher = new VertxExecutionDispatcher(vertx);
        final VertxTimeoutScheduler timeoutScheduler = new VertxTimeoutScheduler(vertx);
        final PendingAsyncResponses pendingAsyncResponses = new PendingAsyncResponses(timeoutScheduler, webServerConfig.exceptionHandler);
        final APP applicationState = VertxRoutesRegistrar.register(router, routesRegisterConsumer, webServerConfig.defaultTimeoutMillis, webServerConfig.exceptionHandler, webServerConfig.maxBodySize, webServerConfig.corsConfig, executionDispatcher, pendingAsyncResponses);

        httpServer.requestHandler(router).listen(webServerConfig.port).toCompletionStage().toCompletableFuture().join();
        return new VertxLuxis<>(executionDispatcher, applicationState, pendingAsyncResponses, () -> vertx.close().toCompletionStage().toCompletableFuture().join());
    }


    @SuppressWarnings("unchecked")
    public static <APP> TestLuxis<APP> test(final ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        final Consumer<Exception>[] ref = new Consumer[]{e -> {}};
        final StubRouter router = new StubRouter(e -> ref[0].accept(e));
        final StubTimeoutScheduler stubTimeoutScheduler = new StubTimeoutScheduler();
        final PendingAsyncResponses pendingAsyncResponses = new PendingAsyncResponses(stubTimeoutScheduler, e -> ref[0].accept(e));
        final RoutesRegister routesRegister = new RoutesRegister(router, new StubExecutionDispatcher(), pendingAsyncResponses);
        final APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestLuxis<>(router, applicationState, ref, pendingAsyncResponses, stubTimeoutScheduler);
    }

    @SuppressWarnings("unchecked")
    public static <APP> TestLuxis<APP> test(final ApplicationRoutesRegister<APP> routesRegisterConsumer, final WebServerConfig webServerConfig) {
        final Consumer<Exception>[] ref = new Consumer[]{webServerConfig.exceptionHandler};
        final StubRouter router = new StubRouter(e -> ref[0].accept(e));
        webServerConfig.corsConfig.ifPresent(router::configureCors);
        router.setMaxBodySize(webServerConfig.maxBodySize);
        final StubTimeoutScheduler stubTimeoutScheduler = new StubTimeoutScheduler();
        final PendingAsyncResponses pendingAsyncResponses = new PendingAsyncResponses(stubTimeoutScheduler, e -> ref[0].accept(e));
        final RoutesRegister routesRegister = new RoutesRegister(router, new StubExecutionDispatcher(), pendingAsyncResponses);
        final APP applicationState = routesRegisterConsumer.registerRoutes(routesRegister);
        return new TestLuxis<>(router, applicationState, ref, pendingAsyncResponses, stubTimeoutScheduler);
    }

    <IN> void apply(final IN immutableState, final BiConsumer<IN, APP> applicationStateConsumer);

    <T> void handleAsyncResponse(long correlationId, Result<HttpErrorResponse, T> result);
}
