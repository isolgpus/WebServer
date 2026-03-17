package io.kiw.web.application.routes;

import io.kiw.web.test.TestApplicationClient;
import io.kiw.web.test.TestApplicationRoutes;
import io.kiw.web.test.TestHttpResponse;
import io.kiw.web.test.StubRequest;
import io.kiw.web.test.jwt.StubJwtProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static io.kiw.web.application.routes.TestApplicationClientCreator.createApplicationClient;
import static io.kiw.web.test.TestHelper.json;
import static org.junit.Assert.assertEquals;

public class JwtAuthTest {

    private TestApplicationClient client;
    private StubJwtProvider jwtProvider;

    @Before
    public void setUp() {
        jwtProvider = new StubJwtProvider(TestApplicationRoutes.JWT_SECRET);
        client = createApplicationClient();
    }

    @After
    public void tearDown() {
        client.assertNoMoreExceptions();
        client.stop();
    }

    @Test
    public void shouldAllowRequestWithValidJwt() {
        String token = jwtProvider.generateToken(Map.of("sub", "user123"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/protected")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json().put("subject", "user123").toString()),
            response);
    }

    @Test
    public void shouldExposeAllClaimsFromToken() {
        String token = jwtProvider.generateToken(Map.of("sub", "user456", "role", "admin"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/protected")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json().put("subject", "user456").toString()),
            response);
    }

    @Test
    public void shouldRejectRequestWithNoAuthorizationHeader() {
        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/protected"));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Missing or invalid Authorization header")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectRequestWithMalformedBearerToken() {
        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/protected")
                .headerParam("Authorization", "Bearer header.payload.badsignature"));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Invalid token signature")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectRequestWithTokenSignedByDifferentSecret() {
        StubJwtProvider otherProvider = new StubJwtProvider("a-completely-different-secret");
        String token = otherProvider.generateToken(Map.of("sub", "attacker"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/protected")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Invalid token signature")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectExpiredToken() {
        long oneHourAgo = System.currentTimeMillis() / 1000 - 3600;
        String token = jwtProvider.generateToken(Map.of("sub", "user789", "exp", oneHourAgo));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/protected")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Token has expired")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectTokenWithWrongHeaderFormat() {
        String token = jwtProvider.generateToken(Map.of("sub", "user123"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/protected")
                .headerParam("Authorization", "Basic " + token));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Missing or invalid Authorization header")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void filterShouldAllowRequestWithValidJwt() {
        String token = jwtProvider.generateToken(Map.of("sub", "user123"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/filter/test")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json().put("subject", "user123").toString()),
            response);
    }

    @Test
    public void filterShouldExposeAllClaimsFromToken() {
        String token = jwtProvider.generateToken(Map.of("sub", "user456", "role", "admin"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/filter/test")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json().put("subject", "user456").toString()),
            response);
    }

    @Test
    public void filterShouldRejectRequestWithNoAuthorizationHeader() {
        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/filter/test"));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Missing or invalid Authorization header")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void filterShouldRejectRequestWithMalformedBearerToken() {
        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/filter/test")
                .headerParam("Authorization", "Bearer header.payload.badsignature"));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Invalid token signature")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void filterShouldRejectRequestWithTokenSignedByDifferentSecret() {
        StubJwtProvider otherProvider = new StubJwtProvider("a-completely-different-secret");
        String token = otherProvider.generateToken(Map.of("sub", "attacker"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/filter/test")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Invalid token signature")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void filterShouldRejectExpiredToken() {
        long oneHourAgo = System.currentTimeMillis() / 1000 - 3600;
        String token = jwtProvider.generateToken(Map.of("sub", "user789", "exp", oneHourAgo));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/filter/test")
                .headerParam("Authorization", "Bearer " + token));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Token has expired")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void filterShouldRejectTokenWithWrongHeaderFormat() {
        String token = jwtProvider.generateToken(Map.of("sub", "user123"));

        TestHttpResponse response = client.get(
            StubRequest.request("/jwt/filter/test")
                .headerParam("Authorization", "Basic " + token));

        assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Missing or invalid Authorization header")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }
}
