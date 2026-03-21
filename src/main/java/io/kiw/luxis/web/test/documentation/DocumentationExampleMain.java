package io.kiw.luxis.web.test.documentation;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.http.Method;

public class DocumentationExampleMain {
    public static void main(final String[] args) {

        Luxis.start(
                routesRegister -> {
                final AppState appState = new AppState();

                routesRegister.jsonRoute("/hello/world", Method.POST, appState, new HelloWorldHandler());
                return appState;
            });

    }
}
