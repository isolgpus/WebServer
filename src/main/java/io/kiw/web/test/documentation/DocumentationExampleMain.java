package io.kiw.web.test.documentation;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;

import io.kiw.web.VertxWebServer;
import io.kiw.web.http.Method;

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
