package io.kiw.web.infrastructure;

import io.kiw.web.ApplicationRoutesRegister;
import io.kiw.web.infrastructure.cors.CorsConfig;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;

public class RoutesRegistrar {

    public static <R> R register(Router router, ApplicationRoutesRegister<R> routesRegisterConsumer, int defaultTimeoutMillis, Consumer<Exception> exceptionHandler, OptionalLong maxBodySize, Optional<CorsConfig> corsConfig) {
        RouterWrapperImpl routerWrapper = new RouterWrapperImpl(router, defaultTimeoutMillis, exceptionHandler);
        corsConfig.ifPresent(routerWrapper::configureCors);

        BodyHandler handler = BodyHandler.create();
        maxBodySize.ifPresent(handler::setBodyLimit);
        router.route().handler(handler);

        RoutesRegister routesRegister = new RoutesRegister(routerWrapper);
        return routesRegisterConsumer.registerRoutes(routesRegister);

    }

}
