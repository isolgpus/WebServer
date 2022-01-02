package io.kiw.template.web.test.documentation;

import io.kiw.template.web.WebServer;
import io.kiw.template.web.infrastructure.Method;

public class DocumentationExampleMain {
    public static void main(String[] args) {

        WebServer.start(8080,
            routesRegister -> {
                AppState appState = new AppState();

                routesRegister.registerJsonRoute("/hello/world", Method.POST, appState.helloWorldState, new HelloWorldHandler());
                return appState;
            });

    }
}
