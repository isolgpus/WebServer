package io.kiw.web.application.routes;

import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.infrastructure.cors.CorsConfigBuilder;
import io.kiw.web.test.StubHttpResponse;
import io.kiw.web.test.StubRequest;
import io.kiw.web.test.TestApplicationClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.kiw.web.test.TestHelper.json;
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
        client = new TestApplicationClient(corsConfig);
    }

    @Test
    public void shouldReturnCorsHeadersOnPreflightRequest() {
        StubHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(204, response.statusCode);
        assertEquals("http://allowed.example.com", response.responseHeaders.get("Access-Control-Allow-Origin"));
        assertEquals("GET,POST,PUT", response.responseHeaders.get("Access-Control-Allow-Methods"));
        assertEquals("Content-Type,Authorization", response.responseHeaders.get("Access-Control-Allow-Headers"));
        assertEquals("true", response.responseHeaders.get("Access-Control-Allow-Credentials"));
        assertEquals("3600", response.responseHeaders.get("Access-Control-Max-Age"));
    }

    @Test
    public void shouldReturnCorsHeadersOnPreflightForSecondAllowedOrigin() {
        StubHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://another.example.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, response.statusCode);
        assertEquals("http://another.example.com", response.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldRejectPreflightFromDisallowedOrigin() {
        StubHttpResponse response = client.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(403, response.statusCode);
        assertNull(response.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldRejectPreflightWithNoOrigin() {
        StubHttpResponse response = client.options(
            StubRequest.request("/echo"));

        assertEquals(403, response.statusCode);
        assertNull(response.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldAddCorsHeadersToNormalGetResponse() {
        StubHttpResponse response = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.responseHeaders.get("Access-Control-Allow-Origin"));
        assertEquals("true", response.responseHeaders.get("Access-Control-Allow-Credentials"));
        assertEquals("X-Custom-Header", response.responseHeaders.get("Access-Control-Expose-Headers"));
    }

    @Test
    public void shouldAddCorsHeadersToNormalPostResponse() {
        StubHttpResponse response = client.post(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.responseHeaders.get("Access-Control-Allow-Origin"));
        assertEquals("true", response.responseHeaders.get("Access-Control-Allow-Credentials"));
    }

    @Test
    public void shouldNotAddCorsHeadersForDisallowedOriginOnNormalRequest() {
        StubHttpResponse response = client.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://evil.example.com"));

        assertEquals(200, response.statusCode);
        assertNull(response.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldNotAddCorsHeadersWhenNoOriginOnNormalRequest() {
        StubHttpResponse response = client.get(
            StubRequest.request("/echo"));

        assertEquals(200, response.statusCode);
        assertNull(response.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldHandleWildcardOrigin() {
        CorsConfig wildcardConfig = new CorsConfigBuilder()
            .allowOrigin("*")
            .allowMethod("GET")
            .build();
        TestApplicationClient wildcardClient = new TestApplicationClient(wildcardConfig);

        StubHttpResponse preflight = wildcardClient.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://any-origin.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, preflight.statusCode);
        assertEquals("*", preflight.responseHeaders.get("Access-Control-Allow-Origin"));

        StubHttpResponse normal = wildcardClient.get(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://any-origin.com"));

        assertEquals(200, normal.statusCode);
        assertEquals("*", normal.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldWorkWithoutCredentialsOrMaxAge() {
        CorsConfig simpleConfig = new CorsConfigBuilder()
            .allowOrigin("http://simple.example.com")
            .allowMethod("GET")
            .build();
        TestApplicationClient simpleClient = new TestApplicationClient(simpleConfig);

        StubHttpResponse response = simpleClient.options(
            StubRequest.request("/echo")
                .headerParam("Origin", "http://simple.example.com")
                .headerParam("Access-Control-Request-Method", "GET"));

        assertEquals(204, response.statusCode);
        assertEquals("http://simple.example.com", response.responseHeaders.get("Access-Control-Allow-Origin"));
        assertNull(response.responseHeaders.get("Access-Control-Allow-Credentials"));
    }

    @Test
    public void shouldNotInterfereWithNormalRequestsWhenNoCorsConfigured() {
        TestApplicationClient noCorsClient = new TestApplicationClient();

        StubHttpResponse response = noCorsClient.get(StubRequest.request("/echo"));

        assertEquals(200, response.statusCode);
        assertNull(response.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldAddCorsHeadersToFilteredRoutes() {
        StubHttpResponse response = client.post(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .body("{}"));

        assertEquals(200, response.statusCode);
        assertEquals("http://allowed.example.com", response.responseHeaders.get("Access-Control-Allow-Origin"));
    }

    @Test
    public void shouldHandlePreflightOnFilteredRoutes() {
        StubHttpResponse response = client.options(
            StubRequest.request("/root/filter/test")
                .headerParam("Origin", "http://allowed.example.com")
                .headerParam("Access-Control-Request-Method", "POST"));

        assertEquals(204, response.statusCode);
        assertEquals("http://allowed.example.com", response.responseHeaders.get("Access-Control-Allow-Origin"));
    }
}
