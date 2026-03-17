package io.kiw.web.application.routes;

import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.infrastructure.cors.CorsConfigBuilder;
import io.kiw.web.test.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.kiw.web.application.routes.TestApplicationClientCreator.createApplicationClient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CorsTest {


    private CorsConfig defaultCorsConfig;
    private TestApplicationClient client;

    @Before
    public void setUp() {
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

        client = createApplicationClient(defaultCorsConfig);

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
        client = createApplicationClient(defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://another.example.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, response.statusCode);
        assertEquals("http://another.example.com", response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldRejectPreflightFromDisallowedOrigin() {
        client = createApplicationClient(defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(403, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldRejectPreflightWithNoOrigin() {
        client = createApplicationClient(defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/echo"));

        assertEquals(405, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldAddCorsHeadersToNormalGetResponse() {
        client = createApplicationClient(defaultCorsConfig);

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
        client = createApplicationClient(defaultCorsConfig);

        TestHttpResponse response = client.post(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{" +
                    "}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
        assertEquals("true", response.getHeader("access-control-allow-credentials"));
    }

    @Test
    public void shouldNotAddCorsHeadersForDisallowedOriginOnNormalRequest() {
        client = createApplicationClient(defaultCorsConfig);

        TestHttpResponse response = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com"));

        assertEquals(403, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldNotAddCorsHeadersWhenNoOriginOnNormalRequest() {
        client = createApplicationClient(defaultCorsConfig);

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
        this.client = createApplicationClient(wildcardConfig);

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
        client = createApplicationClient(simpleConfig);

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
        this.client = createApplicationClient();

        TestHttpResponse response = client.get(StubRequest.request("/echo"));

        assertEquals(200, response.statusCode);
        assertNull(response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldAddCorsHeadersToFilteredRoutes() {
        client = createApplicationClient(defaultCorsConfig);

        TestHttpResponse response = client.post(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{" +
                    "}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
    }

    @Test
    public void shouldHandlePreflightOnFilteredRoutes() {
        client = createApplicationClient(defaultCorsConfig);

        TestHttpResponse response = client.options(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(204, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("access-control-allow-origin"));
    }


    @After
    public void tearDown() {
        client.stop();
    }

}
