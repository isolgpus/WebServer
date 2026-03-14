package io.kiw.web.application.routes;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;
import io.kiw.web.test.StubHttpResponse;
import io.kiw.web.test.StubRequest;
import io.kiw.web.test.StubRouter;
import io.kiw.web.test.jwt.StubJwtProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.kiw.web.test.TestHelper.json;
import static org.junit.Assert.assertEquals;

public class JwtAuthTest {

    private static final String SECRET = "test-secret-key-for-unit-tests";

    private StubJwtProvider jwtProvider;
    private StubRouter router;
    private List<Exception> exceptions;

    public static class SubjectResponse {
        public final String subject;
        public SubjectResponse(String subject) { this.subject = subject; }
    }

    @Before
    public void setUp() {
        jwtProvider = new StubJwtProvider(SECRET);
        exceptions = new ArrayList<>();
        router = new StubRouter(exceptions::add);

        RoutesRegister routesRegister = new RoutesRegister(router);
        MyApplicationState state = new MyApplicationState();

        routesRegister.jsonRoute("/protected", Method.GET, state,
            new VertxJsonRoute<EmptyRequest, SubjectResponse, MyApplicationState>() {
                @Override
                public RequestPipeline<SubjectResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> stream) {
                    return stream
                        .requireJwt(jwtProvider)
                        .complete((req, ctx, app) ->
                            HttpResult.success(new SubjectResponse(ctx.getJwtClaims().getSubject())));
                }
            });
    }

    @Test
    public void shouldAllowRequestWithValidJwt() {
        String token = jwtProvider.generateToken(Map.of("sub", "user123"));

        StubHttpResponse response = router.handle(
            StubRequest.request("/protected").headerParam("Authorization", "Bearer " + token),
            Method.GET);

        assertEquals(
            StubHttpResponse.response(json().put("subject", "user123").toString()),
            response);
    }

    @Test
    public void shouldExposeAllClaimsFromToken() {
        String token = jwtProvider.generateToken(Map.of("sub", "user456", "role", "admin"));

        StubHttpResponse response = router.handle(
            StubRequest.request("/protected").headerParam("Authorization", "Bearer " + token),
            Method.GET);

        assertEquals(
            StubHttpResponse.response(json().put("subject", "user456").toString()),
            response);
    }

    @Test
    public void shouldRejectRequestWithNoAuthorizationHeader() {
        StubHttpResponse response = router.handle(
            StubRequest.request("/protected"),
            Method.GET);

        assertEquals(
            StubHttpResponse.response(json()
                .put("message", "Missing or invalid Authorization header")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectRequestWithMalformedBearerToken() {
        StubHttpResponse response = router.handle(
            StubRequest.request("/protected").headerParam("Authorization", "Bearer header.payload.badsignature"),
            Method.GET);

        assertEquals(
            StubHttpResponse.response(json()
                .put("message", "Invalid token signature")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectRequestWithTokenSignedByDifferentSecret() {
        StubJwtProvider otherProvider = new StubJwtProvider("a-completely-different-secret");
        String token = otherProvider.generateToken(Map.of("sub", "attacker"));

        StubHttpResponse response = router.handle(
            StubRequest.request("/protected").headerParam("Authorization", "Bearer " + token),
            Method.GET);

        assertEquals(
            StubHttpResponse.response(json()
                .put("message", "Invalid token signature")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectExpiredToken() {
        long oneHourAgo = System.currentTimeMillis() / 1000 - 3600;
        String token = jwtProvider.generateToken(Map.of("sub", "user789", "exp", oneHourAgo));

        StubHttpResponse response = router.handle(
            StubRequest.request("/protected").headerParam("Authorization", "Bearer " + token),
            Method.GET);

        assertEquals(
            StubHttpResponse.response(json()
                .put("message", "Token has expired")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }

    @Test
    public void shouldRejectTokenWithWrongHeaderFormat() {
        String token = jwtProvider.generateToken(Map.of("sub", "user123"));

        StubHttpResponse response = router.handle(
            StubRequest.request("/protected").headerParam("Authorization", "Basic " + token),
            Method.GET);

        assertEquals(
            StubHttpResponse.response(json()
                .put("message", "Missing or invalid Authorization header")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response);
    }
}
