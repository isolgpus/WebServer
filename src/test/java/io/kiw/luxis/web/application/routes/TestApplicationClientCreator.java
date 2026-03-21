package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.WebServerConfig;
import io.kiw.luxis.web.WebServiceConfigBuilder;
import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.internal.RoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.test.StubTestApplicationClient;
import io.kiw.luxis.web.test.TestApplicationClient;
import io.kiw.luxis.web.test.VertxHttpTestApplicationClient;
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
            Luxis<MyApplicationState> luxis = Luxis.start(routesRegister -> {
                registerRoutes.accept(routesRegister, state);
                return state;
            }, config);
            return new VertxHttpTestApplicationClient("127.0.0.1", 8080, luxis);
        }
        WebServiceConfigBuilder builder = new WebServiceConfigBuilder();
        if (corsConfig != null) {
            builder.setCorsConfig(corsConfig);
        }
        Luxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            registerRoutes.accept(routesRegister, state);
            return state;
        }, builder.build());
        return new StubTestApplicationClient("127.0.0.1", 8080, luxis);
    }
}
