package io.kiw.template.web.test;

import io.kiw.template.web.WebServer;
import io.kiw.template.web.WebServiceConfigBuilder;

public class WebServerTestMain {
    public static void main(String[] args) {

        WebServer<MyApplicationState> webServer = WebServer.start(
            TestApplicationClient::registerRoutes, new WebServiceConfigBuilder()
                .setPort(8080)
                .setDefaultBlockingTimeoutMillis(5000)
                .setExceptionHandler(Throwable::printStackTrace)
                .setMaxBodySize(1_048_576)
                .build());

        webServer.apply(82876, (event, myApplicationState) -> myApplicationState.setLongValue(event));
    }
}
