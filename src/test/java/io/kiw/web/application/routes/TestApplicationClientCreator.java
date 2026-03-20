package io.kiw.web.application.routes;

import io.kiw.web.WebServer;
import io.kiw.web.WebServerConfig;
import io.kiw.web.WebServiceConfigBuilder;
import io.kiw.web.infrastructure.RoutesRegister;
import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.test.*;
import org.junit.Assume;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;

public class TestApplicationClientCreator {

    public static final String STUB_MODE = "stub";
    public static final String REAL_MODE = "real";

    public static Collection<Object[]> modes() {
        return Arrays.asList(new Object[][]{{STUB_MODE}, {REAL_MODE}});
    }

    public static void assumeRealModeEnabled() {
        Assume.assumeTrue(
            "Skipping real server test: set TEST_MODE=REAL_WEB_SERVER to enable",
            "REAL_WEB_SERVER".equals(System.getenv("TEST_MODE")));
    }

    public static TestApplicationClient createClient(String mode, BiConsumer<RoutesRegister, MyApplicationState> registerRoutes) {
        return createClient(mode, registerRoutes, null);
    }

    public static TestApplicationClient createClient(String mode, BiConsumer<RoutesRegister, MyApplicationState> registerRoutes, CorsConfig corsConfig) {
        MyApplicationState state = new MyApplicationState();
        if (REAL_MODE.equals(mode)) {
            WebServiceConfigBuilder builder = new WebServiceConfigBuilder().setPort(8080);
            if (corsConfig != null) {
                builder.setCorsConfig(corsConfig);
            }
            WebServerConfig config = builder.build();
            WebServer<MyApplicationState> webServer = WebServer.start(routesRegister -> {
                registerRoutes.accept(routesRegister, state);
                return state;
            }, config);
            return new VertxHttpTestApplicationClient("127.0.0.1", 8080, webServer);
        }
        return new StubTestApplicationClient(r -> registerRoutes.accept(r, state), corsConfig);
    }
}
