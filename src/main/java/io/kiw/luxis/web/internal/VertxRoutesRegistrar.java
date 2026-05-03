package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.ApplicationRoutesRegister;
import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.db.DatabaseClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;

public final class VertxRoutesRegistrar {
    private VertxRoutesRegistrar() {
    }

    public static <R> R register(final Router router,
                                 final ApplicationRoutesRegister<R> routesRegisterConsumer,
                                 final int defaultTimeoutMillis,
                                 final Consumer<Exception> exceptionHandler,
                                 final OptionalLong maxBodySize,
                                 final Optional<CorsConfig> corsConfig,
                                 final VertxExecutionDispatcher executionDispatcher,
                                 final PendingAsyncResponses pendingAsyncResponses) {
        return register(router, routesRegisterConsumer, defaultTimeoutMillis, exceptionHandler, maxBodySize, corsConfig, executionDispatcher, pendingAsyncResponses, null);
    }

    public static <R> R register(final Router router,
                                 final ApplicationRoutesRegister<R> routesRegisterConsumer,
                                 final int defaultTimeoutMillis,
                                 final Consumer<Exception> exceptionHandler,
                                 final OptionalLong maxBodySize,
                                 final Optional<CorsConfig> corsConfig,
                                 final VertxExecutionDispatcher executionDispatcher,
                                 final PendingAsyncResponses pendingAsyncResponses,
                                 final DatabaseClient<?, ?, ?> databaseClient) {
        final TransactionExecutor transactionExecutor = databaseClient == null ? null : new TransactionExecutor(databaseClient, executionDispatcher);
        final VertxRouterWrapperImpl routerWrapper = new VertxRouterWrapperImpl(router, defaultTimeoutMillis, exceptionHandler, pendingAsyncResponses, transactionExecutor, databaseClient);
        corsConfig.ifPresent(routerWrapper::configureCors);

        final BodyHandler handler = BodyHandler.create()
                .setDeleteUploadedFilesOnEnd(true);
        maxBodySize.ifPresent(handler::setBodyLimit);
        router.route().handler(handler);

        final RoutesRegister routesRegister = new RoutesRegister(routerWrapper, executionDispatcher, pendingAsyncResponses, databaseClient);
        return routesRegisterConsumer.registerRoutes(routesRegister);

    }

}
