package io.kiw.template.web;

import io.kiw.template.web.test.handler.BlockingTestHandler;
import io.kiw.template.web.test.handler.FailingTestHandler;
import io.kiw.template.web.test.handler.GetEchoHandler;
import io.kiw.template.web.test.handler.PostEchoHandler;

import static io.kiw.template.web.infrastructure.Method.GET;
import static io.kiw.template.web.infrastructure.Method.POST;

public class WebTemplateMain {
    public static void main(String[] args) {

        WebServer.start(8080, routesRegister -> {
            routesRegister.registerJsonRoute("/echo", POST, new PostEchoHandler());
            routesRegister.registerJsonRoute("/echo", GET, new GetEchoHandler());
            routesRegister.registerJsonRoute("/blocking", POST, new BlockingTestHandler());
            routesRegister.registerJsonRoute("/failing", POST, new FailingTestHandler());
        });

    }
}
