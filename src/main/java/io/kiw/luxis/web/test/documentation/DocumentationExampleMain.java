package io.kiw.luxis.web.test.documentation;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.VertxLuxis;
import io.kiw.luxis.web.http.Method;

public class DocumentationExampleMain {
    public static void main(String[] args) {

        Luxis.start(
            routesRegister -> {
                AppState appState = new AppState();

                routesRegister.jsonRoute("/hello/world", Method.POST, appState.helloWorldState, new HelloWorldHandler());
                return appState;
            });

    }
}
