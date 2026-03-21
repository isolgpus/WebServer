package io.kiw.luxis.web.test;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.WebServiceConfigBuilder;

public class WebServerTestMain {
    public static void main(String[] args) {

        Luxis<MyApplicationState> luxis = Luxis.start(
            routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()), new WebServiceConfigBuilder()
                .setPort(8080)
                .setDefaultBlockingTimeoutMillis(5000)
                .setExceptionHandler(Throwable::printStackTrace)
                .setMaxBodySize(1_048_576)
                .build());

        luxis.apply(82876, (event, myApplicationState) -> myApplicationState.setLongValue(event));
    }
}
