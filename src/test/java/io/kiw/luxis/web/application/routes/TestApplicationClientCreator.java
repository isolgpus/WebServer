package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.ApplicationRoutesRegister;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.WebServerConfig;
import io.kiw.luxis.web.WebServiceConfigBuilder;
import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.internal.RoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.test.StubTestClient;
import io.kiw.luxis.web.test.TestClient;
import io.kiw.luxis.web.test.VertxTestClient;
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
            "Skipping real server test: set TEST_MODE=VERTX to enable",
            "VERTX".equals(System.getenv("TEST_MODE")));
    }

    public static TestClient createClient(String mode, BiConsumer<RoutesRegister, MyApplicationState> registerRoutes) {
        return createClient(mode, registerRoutes, null);
    }

    public static TestClient createClient(String mode, BiConsumer<RoutesRegister, MyApplicationState> registerRoutes, CorsConfig corsConfig) {
        MyApplicationState state = new MyApplicationState();
        WebServiceConfigBuilder builder = new WebServiceConfigBuilder().setPort(8080);
        if (corsConfig != null) {
            builder.setCorsConfig(corsConfig);
        }
        WebServerConfig config = builder.build();

        ApplicationRoutesRegister<MyApplicationState> routes = routesRegister -> {
            registerRoutes.accept(routesRegister, state);
            return state;
        };

        if (REAL_MODE.equals(mode)) {

            Luxis<MyApplicationState> luxis = Luxis.start(routes, config);
            return new VertxTestClient("127.0.0.1", 8080, luxis);
        }
        else
        {
            Luxis<MyApplicationState> luxis = Luxis.test(routes, config);
            return new StubTestClient("127.0.0.1", 8080, luxis);
        }

    }
}
