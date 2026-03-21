package io.kiw.luxis.web.test.documentation;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;

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
