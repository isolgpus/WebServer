package io.kiw.template.web.infrastructure;

import io.kiw.template.web.ApplicationRoutesRegister;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class RoutesRegistrar {

    public static <R> R register(Router router, ApplicationRoutesRegister<R> routesRegisterConsumer, int defaultTimeoutMillis) {
        router.route().handler(BodyHandler.create());
        RoutesRegister routesRegister = new RoutesRegister(new RouterWrapperImpl(router, defaultTimeoutMillis));
        return routesRegisterConsumer.registerRoutes(routesRegister);

    }

}
