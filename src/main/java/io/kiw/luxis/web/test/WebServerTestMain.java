package io.kiw.luxis.web.test;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;
import io.kiw.luxis.web.openapi.*;

import io.kiw.luxis.web.VertxWebServer;
import io.kiw.luxis.web.WebServer;
import io.kiw.luxis.web.WebServiceConfigBuilder;

public class WebServerTestMain {
    public static void main(String[] args) {

        WebServer<MyApplicationState> webServer = VertxWebServer.start(
            routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()), new WebServiceConfigBuilder()
                .setPort(8080)
                .setDefaultBlockingTimeoutMillis(5000)
                .setExceptionHandler(Throwable::printStackTrace)
                .setMaxBodySize(1_048_576)
                .build());

        webServer.apply(82876, (event, myApplicationState) -> myApplicationState.setLongValue(event));
    }
}
