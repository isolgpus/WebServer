package io.kiw.template.web.test;

import io.kiw.template.web.WebServer;

public class WebServerTestMain {
    public static void main(String[] args) {

        WebServer<MyApplicationState> webServer = WebServer.start(8080,
            TestApplicationClient::registerRoutes);

        webServer.run(myApplicationState -> myApplicationState.setLongValue(82876));
    }
}
