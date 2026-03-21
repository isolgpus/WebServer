package io.kiw.web.test;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;
import io.kiw.web.openapi.*;

import io.kiw.web.VertxWebServer;
import io.kiw.web.WebServer;
import io.kiw.web.WebServiceConfigBuilder;

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
