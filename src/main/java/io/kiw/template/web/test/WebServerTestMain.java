package io.kiw.template.web.test;

import io.kiw.template.web.WebServer;

public class WebServerTestMain {
    public static void main(String[] args) {

        WebServer<MyApplicationState> webServer = WebServer.start(
            TestApplicationClient::registerRoutes);

        webServer.apply(82876, (immutableState, myApplicationState) -> myApplicationState.setLongValue(immutableState));
    }
}
