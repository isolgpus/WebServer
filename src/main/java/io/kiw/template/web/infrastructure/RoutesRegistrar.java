package io.kiw.template.web.infrastructure;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.function.Consumer;

public class RoutesRegistrar {

    public static void register(Router router, Consumer<RoutesRegister> routesRegisterConsumer) {
        router.route().handler(BodyHandler.create());
        RoutesRegister routesRegister = new RoutesRegister(new RouterWrapperImpl(router));
        routesRegisterConsumer.accept(routesRegister);

    }

}
