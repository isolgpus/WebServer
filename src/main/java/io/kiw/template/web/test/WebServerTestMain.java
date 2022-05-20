package io.kiw.template.web.test;

import io.kiw.template.web.WebServer;

public class WebServerTestMain {
    public static void main(String[] args) {

        WebServer<MyApplicationState> webServer = WebServer.start(
            TestApplicationClient::registerRoutes, new WebServiceConfigBuilder()
                .setPort(8080)
                .setDefaultBlockingTimeoutMillis(5000)
                .build());

        webServer.apply(82876, (event, myApplicationState) -> myApplicationState.setLongValue(event));
    }
}
