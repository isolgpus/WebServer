package io.kiw.web.test.documentation;

import io.kiw.web.VertxWebServer;
import io.kiw.web.infrastructure.Method;

public class DocumentationExampleMain {
    public static void main(String[] args) {

        VertxWebServer.start(
            routesRegister -> {
                AppState appState = new AppState();

                routesRegister.jsonRoute("/hello/world", Method.POST, appState.helloWorldState, new HelloWorldHandler());
                return appState;
            });

    }
}
