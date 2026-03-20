package io.kiw.web;

import java.util.function.BiConsumer;

public class TestWebServer<APP> implements WebServer<APP> {

    public static <APP> WebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        return new TestWebServer<>();
    }

    public static <APP> WebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer, WebServerConfig webServerConfig) {
        return new TestWebServer<>();
    }

    @Override
    public <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer) {
    }

    @Override
    public void stop() {
    }
}
