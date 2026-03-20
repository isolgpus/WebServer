package io.kiw.web.application.routes;

import io.kiw.web.infrastructure.Method;
import io.kiw.web.test.*;
import io.kiw.web.test.handler.JwtFilterProtectedHandler;
import io.kiw.web.test.handler.JwtProtectedHandler;
import io.kiw.web.test.jwt.StubJwtProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Map;

import static io.kiw.web.application.routes.TestApplicationClientCreator.*;
import static io.kiw.web.test.TestHelper.json;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class JwtAuthTest {

    private static final String JWT_SECRET = TestApplicationRoutes.JWT_SECRET;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestApplicationClient client;
    private StubJwtProvider jwtProvider;

    public JwtAuthTest(String mode) {
        this.mode = mode;
    }

    @Before
    public void setUp() {
        if (REAL_MODE.equals(mode)) {
            assumeRealModeEnabled();
        }
        jwtProvider = new StubJwtProvider(JWT_SECRET);
    }

    @After
    public void tearDown() {
        if (client != null) {
            client.assertNoMoreExceptions();
            client.stop();
        }
    }

    @Test
    public void shouldAllowRequestWithValidJwt() {
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(new StubJwtProvider(JWT_SECRET)));
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(new StubJwtProvider(JWT_SECRET)));
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(new StubJwtProvider(JWT_SECRET)));
        });

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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(new StubJwtProvider(JWT_SECRET)));
        });

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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(new StubJwtProvider(JWT_SECRET)));
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(new StubJwtProvider(JWT_SECRET)));
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/jwt/protected", Method.GET, state, new JwtProtectedHandler(new StubJwtProvider(JWT_SECRET)));
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonFilter("/jwt/filter/*", state, new JwtFilter(new StubJwtProvider(JWT_SECRET)));
            r.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonFilter("/jwt/filter/*", state, new JwtFilter(new StubJwtProvider(JWT_SECRET)));
            r.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonFilter("/jwt/filter/*", state, new JwtFilter(new StubJwtProvider(JWT_SECRET)));
            r.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());
        });

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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonFilter("/jwt/filter/*", state, new JwtFilter(new StubJwtProvider(JWT_SECRET)));
            r.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());
        });

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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonFilter("/jwt/filter/*", state, new JwtFilter(new StubJwtProvider(JWT_SECRET)));
            r.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonFilter("/jwt/filter/*", state, new JwtFilter(new StubJwtProvider(JWT_SECRET)));
            r.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());
        });
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
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonFilter("/jwt/filter/*", state, new JwtFilter(new StubJwtProvider(JWT_SECRET)));
            r.jsonRoute("/jwt/filter/test", Method.GET, state, new JwtFilterProtectedHandler());
        });
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
