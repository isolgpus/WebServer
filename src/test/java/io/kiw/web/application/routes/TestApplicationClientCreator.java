package io.kiw.web.application.routes;

import io.kiw.web.WebServer;
import io.kiw.web.WebServiceConfigBuilder;
import io.kiw.web.test.*;

public class TestApplicationClientCreator {
    public static ApplicationClient createApplicationClient() {

        if("REAL_WEB_SERVER".equals(System.getenv("TEST_MODE")))
        {
            WebServer<MyApplicationState> start = WebServer.start(routesRegister -> {
                MyApplicationState state = new MyApplicationState();
                TestApplicationRoutes.registerRoutes(routesRegister, state);
                return state;
            }, new WebServiceConfigBuilder().setPort(8080).build());
            return new VertxHttpApplicationClient("127.0.0.1", 8080);
        }
        else
        {
            return new TestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()));
        }
    }
}
