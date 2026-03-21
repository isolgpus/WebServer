package io.kiw.luxis.web.test.documentation;

import io.kiw.luxis.web.VertxWebServer;
import io.kiw.luxis.web.http.Method;

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
