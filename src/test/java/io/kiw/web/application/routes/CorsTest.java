package io.kiw.web.application.routes;

import io.kiw.web.http.Method;
import io.kiw.web.cors.CorsConfig;
import io.kiw.web.cors.CorsConfigBuilder;
import io.kiw.web.test.*;
import io.kiw.web.test.handler.GetEchoHandler;
import io.kiw.web.test.handler.PostEchoHandler;
import io.kiw.web.test.handler.TestFilterHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.web.application.routes.TestApplicationClientCreator.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class CorsTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private CorsConfig defaultCorsConfig;
    private TestApplicationClient client;

    public CorsTest(String mode) {
        this.mode = mode;
    }

    @Before
    public void setUp() {
        if (REAL_MODE.equals(mode)) {
            assumeRealModeEnabled();
        }
        defaultCorsConfig = new CorsConfigBuilder()
            .allowOrigin("http://allowed.example.com")
            .allowOrigin("http://another.example.com")
            .allowMethod("GET")
            .allowMethod("POST")
            .allowMethod("PUT")
            .allowHeader("Content-Type")
            .allowHeader("Authorization")
            .exposeHeader("X-Custom-Header")
            .allowCredentials(true)
            .maxAgeSeconds(3600)
            .build();
    }

    @Test
    public void shouldReturnCorsHeadersOnPreflightRequest() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(204, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
        assertEquals("GET,POST,PUT", response.getHeader("access-control-allow-methods"));
        assertEquals("Content-Type,Authorization", response.getHeader("access-control-allow-headers"));
        assertEquals("true", response.getHeader("access-control-allow-credentials"));
        assertEquals("3600", response.getHeader("access-control-max-age"));
    }

    @Test
    public void shouldReturnCorsHeadersOnPreflightForSecondAllowedOrigin() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://another.example.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, response.statusCode);
        assertEquals("http://another.example.com", response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldRejectPreflightFromDisallowedOrigin() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(403, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldRejectPreflightWithNoOrigin() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo"));

        assertEquals(405, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldAddCorsHeadersToNormalGetResponse() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
        assertEquals("true", response.getHeader("access-control-allow-credentials"));
        assertEquals("X-Custom-Header", response.getHeader("access-control-expose-headers"));
    }

    @Test
    public void shouldAddCorsHeadersToNormalPostResponse() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.post(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
        assertEquals("true", response.getHeader("access-control-allow-credentials"));
    }

    @Test
    public void shouldNotAddCorsHeadersForDisallowedOriginOnNormalRequest() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com"));

        assertEquals(403, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldNotAddCorsHeadersWhenNoOriginOnNormalRequest() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.get(
            StubRequest.request("/echo"));

        assertEquals(200, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldHandleWildcardOrigin() {
        CorsConfig wildcardConfig = new CorsConfigBuilder()
            .allowOrigin("*")
            .allowMethod("GET")
            .build();
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, wildcardConfig);

        TestHttpResponse preflight = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://any-origin.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, preflight.statusCode);
        assertEquals("*", preflight.getHeader("access-control-allow-origin"));

        TestHttpResponse normal = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://any-origin.com"));

        assertEquals(200, normal.statusCode);
        assertEquals("*", normal.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldWorkWithoutCredentialsOrMaxAge() {
        CorsConfig simpleConfig = new CorsConfigBuilder()
            .allowOrigin("http://simple.example.com")
            .allowMethod("GET")
            .build();
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        }, simpleConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://simple.example.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, response.statusCode);
        assertEquals("http://simple.example.com", response.getHeader("access-control-allow-origin"));
        assertNull(response.getHeader("access-control-allow-credentials"));
    }

    @Test
    public void shouldNotInterfereWithNormalRequestsWhenNoCorsConfigured() {
        client = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        });

        TestHttpResponse response = client.get(StubRequest.request("/echo"));

        assertEquals(200, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldAddCorsHeadersToFilteredRoutes() {
        client = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.POST, state, new TestFilterHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.post(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldHandlePreflightOnFilteredRoutes() {
        client = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.POST, state, new TestFilterHandler());
        }, defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(204, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
    }


    @After
    public void tearDown() {
        if (client != null) {
            client.stop();
        }
    }

}
