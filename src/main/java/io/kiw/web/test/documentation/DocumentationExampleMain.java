package io.kiw.web.test.documentation;

import io.kiw.web.WebServer;
import io.kiw.web.infrastructure.Method;

public class DocumentationExampleMain {
    public static void main(String[] args) {

        WebServer.start(
            routesRegister -> {
                AppState appState = new AppState();

                routesRegister.jsonRoute("/hello/world", Method.POST, appState.helloWorldState, new HelloWorldHandler());
                return appState;
            });

    }
}
