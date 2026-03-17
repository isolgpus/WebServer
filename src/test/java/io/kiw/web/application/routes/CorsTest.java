package io.kiw.web.application.routes;

import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.infrastructure.cors.CorsConfigBuilder;
import io.kiw.web.test.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CorsTest {

    private TestApplicationClient client;

    @Before
    public void setUp() {
        CorsConfig corsConfig = new CorsConfigBuilder()
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
        client = new StubTestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()), corsConfig);
    }

    @Test
    public void shouldReturnCorsHeadersOnPreflightRequest() {
        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(204, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("GET,POST,PUT", response.getHeader("Access-Control-Allow-Methods"));
        assertEquals("Content-Type,Authorization", response.getHeader("Access-Control-Allow-Headers"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
    }

    @Test
    public void shouldReturnCorsHeadersOnPreflightForSecondAllowedOrigin() {
        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://another.example.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, response.statusCode);
        assertEquals("http://another.example.com", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldRejectPreflightFromDisallowedOrigin() {
        TestHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(403, response.statusCode);
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldRejectPreflightWithNoOrigin() {
        TestHttpResponse response = client.options(
            StubRequest.request("/echo"));

        assertEquals(403, response.statusCode);
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldAddCorsHeadersToNormalGetResponse() {
        TestHttpResponse response = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
        assertEquals("X-Custom-Header", response.getHeader("Access-Control-Expose-Headers"));
    }

    @Test
    public void shouldAddCorsHeadersToNormalPostResponse() {
        TestHttpResponse response = client.post(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
    }

    @Test
    public void shouldNotAddCorsHeadersForDisallowedOriginOnNormalRequest() {
        TestHttpResponse response = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com"));

        assertEquals(200, response.statusCode);
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldNotAddCorsHeadersWhenNoOriginOnNormalRequest() {
        TestHttpResponse response = client.get(
            StubRequest.request("/echo"));

        assertEquals(200, response.statusCode);
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldHandleWildcardOrigin() {
        CorsConfig wildcardConfig = new CorsConfigBuilder()
            .allowOrigin("*")
            .allowMethod("GET")
            .build();
        TestApplicationClient wildcardClient = new StubTestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()), wildcardConfig);

        TestHttpResponse preflight = wildcardClient.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://any-origin.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, preflight.statusCode);
        assertEquals("*", preflight.getHeader("Access-Control-Allow-Origin"));

        TestHttpResponse normal = wildcardClient.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://any-origin.com"));

        assertEquals(200, normal.statusCode);
        assertEquals("*", normal.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldWorkWithoutCredentialsOrMaxAge() {
        CorsConfig simpleConfig = new CorsConfigBuilder()
            .allowOrigin("http://simple.example.com")
            .allowMethod("GET")
            .build();
        TestApplicationClient simpleClient = new StubTestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()), simpleConfig);

        TestHttpResponse response = simpleClient.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://simple.example.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, response.statusCode);
        assertEquals("http://simple.example.com", response.getHeader("Access-Control-Allow-Origin"));
        assertNull(response.getHeader("Access-Control-Allow-Credentials"));
    }

    @Test
    public void shouldNotInterfereWithNormalRequestsWhenNoCorsConfigured() {
        TestApplicationClient noCorsClient = new StubTestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()));

        TestHttpResponse response = noCorsClient.get(StubRequest.request("/echo"));

        assertEquals(200, response.statusCode);
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldAddCorsHeadersToFilteredRoutes() {
        TestHttpResponse response = client.post(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldHandlePreflightOnFilteredRoutes() {
        TestHttpResponse response = client.options(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(204, response.statusCode);
        assertEquals("http://allowed.example.com", response.getHeader("Access-Control-Allow-Origin"));
    }
}
