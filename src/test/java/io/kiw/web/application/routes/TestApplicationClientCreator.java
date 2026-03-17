package io.kiw.web.application.routes;

import io.kiw.web.WebServer;
import io.kiw.web.WebServerConfig;
import io.kiw.web.WebServiceConfigBuilder;
import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.test.*;

public class TestApplicationClientCreator {

    public static TestApplicationClient createApplicationClient(final CorsConfig corsConfig) {

        if("REAL_WEB_SERVER".equals(System.getenv("TEST_MODE")))
        {
            WebServiceConfigBuilder builder = new WebServiceConfigBuilder().setPort(8080);
            if(corsConfig != null) {
                builder.setCorsConfig(corsConfig);
            }

            WebServerConfig config = builder.build();

            WebServer<MyApplicationState> webServer = WebServer.start(routesRegister -> {
                MyApplicationState state = new MyApplicationState();
                TestApplicationRoutes.registerRoutes(routesRegister, state);
                return state;
            }, config);

            return new VertxHttpTestApplicationClient("127.0.0.1", 8080, webServer);
        }
        else
        {
            return new StubTestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()), corsConfig);
        }
    }

    public static TestApplicationClient createApplicationClient() {
        return createApplicationClient(null);
    }
}
