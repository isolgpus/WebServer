package io.kiw.template.web.test;

import io.kiw.template.web.WebServer;

public class WebServerTestMain {
    public static void main(String[] args) {

        WebServer<MyApplicationState> webServer = WebServer.start(8080, (routesRegister) ->
        {
            MyApplicationState myApplicationState = new MyApplicationState();

            TestApplicationClient.registerRoutes(routesRegister, myApplicationState);

            return myApplicationState;
        });

        webServer.run(myApplicationState -> myApplicationState.setLongValue(82876));
    }
}
