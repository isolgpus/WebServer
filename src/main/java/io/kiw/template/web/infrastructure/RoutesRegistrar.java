package io.kiw.template.web.infrastructure;

import io.kiw.template.web.ApplicationRoutesRegister;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.function.Consumer;

public class RoutesRegistrar {

    public static <R> R register(Router router, ApplicationRoutesRegister<R> routesRegisterConsumer, int defaultTimeoutMillis, Consumer<Exception> exceptionHandler) {
        router.route().handler(BodyHandler.create());
        RoutesRegister routesRegister = new RoutesRegister(new RouterWrapperImpl(router, defaultTimeoutMillis, exceptionHandler));
        return routesRegisterConsumer.registerRoutes(routesRegister);

    }

}
