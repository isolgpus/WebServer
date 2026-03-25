package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.ApplicationRoutesRegister;
import io.kiw.luxis.web.cors.CorsConfig;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;

public final class VertxRoutesRegistrar {
    private VertxRoutesRegistrar() { }

    public static <R> R register(final Router router,
                                 final ApplicationRoutesRegister<R> routesRegisterConsumer,
                                 final int defaultTimeoutMillis,
                                 final Consumer<Exception> exceptionHandler,
                                 final OptionalLong maxBodySize,
                                 final Optional<CorsConfig> corsConfig,
                                 final VertxExecutionDispatcher executionDispatcher,
                                 final PendingAsyncResponses pendingAsyncResponses) {
        final VertxRouterWrapperImpl routerWrapper = new VertxRouterWrapperImpl(router, defaultTimeoutMillis, exceptionHandler);
        corsConfig.ifPresent(routerWrapper::configureCors);

        final BodyHandler handler = BodyHandler.create()
                .setDeleteUploadedFilesOnEnd(true);
        maxBodySize.ifPresent(handler::setBodyLimit);
        router.route().handler(handler);

        final RoutesRegister routesRegister = new RoutesRegister(routerWrapper, executionDispatcher, pendingAsyncResponses);
        return routesRegisterConsumer.registerRoutes(routesRegister);

    }

}
