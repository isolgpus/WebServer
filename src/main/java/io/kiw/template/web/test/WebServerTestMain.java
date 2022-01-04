package io.kiw.template.web.test;

import io.kiw.template.web.WebServer;
import io.kiw.template.web.WebServerConfig;

public class WebServerTestMain {
    public static void main(String[] args) {

        WebServer<MyApplicationState> webServer = WebServer.start(
            TestApplicationClient::registerRoutes, new WebServerConfig()
                        .setPort(8080)
                        .setErrorHandler(exception -> {
                            System.out.println(exception.getMessage());
                        }));

        webServer.apply(82876, (immutableState, myApplicationState) -> myApplicationState.setLongValue(immutableState));
    }
}
