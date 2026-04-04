package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.http.client.LuxisHttpClient;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.test.handler.ErrorHandler;
import io.kiw.luxis.web.test.handler.HttpClientCallHandler;
import io.kiw.luxis.web.test.handler.HttpClientChainedCallHandler;
import io.kiw.luxis.web.test.handler.HttpClientDeleteCallHandler;
import io.kiw.luxis.web.test.handler.HttpClientPostCallHandler;
import io.kiw.luxis.web.test.handler.HttpClientPutCallHandler;
import io.kiw.luxis.web.test.handler.SimpleGetHandler;
import io.kiw.luxis.web.test.handler.SimpleMultiplyHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.REAL_MODE;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.assumeRealModeEnabled;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.createTestServerAndClient;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.createHttpClient;
import static io.kiw.luxis.web.test.TestHelper.json;

@RunWith(Parameterized.class)
public class HttpClientTest {

    private static final String SERVER_B_HOST = "127.0.0.1";
    private static final int SERVER_B_PORT = 8081;
    private static final String SERVER_B_BASE_URL = "http://" + SERVER_B_HOST + ":" + SERVER_B_PORT;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestClientAndServer serverA;
    private TestClientAndServer serverB;

    public HttpClientTest(String mode) {
        this.mode = mode;
    }

    @Before
    public void assumeMode() {
        if (REAL_MODE.equals(mode)) {
            assumeRealModeEnabled();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (serverA != null) {
            serverA.close();
        }
        if (serverB != null) {
            serverB.close();
        }
    }

    @Test
    public void shouldCallServerBViaHttpClientGet() {
        serverB = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/api/value", Method.GET, state, new SimpleGetHandler(42)),
                builder -> builder.setPort(SERVER_B_PORT));

        final LuxisHttpClient httpClient = createHttpClient(mode, serverB, SERVER_B_HOST, SERVER_B_PORT);

        serverA = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/call-b", Method.POST, state, new HttpClientCallHandler(httpClient, SERVER_B_BASE_URL)));

        final TestHttpResponse response = serverA.client().post(
                StubRequest.request("/call-b")
                        .body(json().put("targetPath", "/api/value").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json()
                        .put("statusCode", 200)
                        .put("body", json().put("result", 42).toString())
                        .toString()),
                response);
    }

    @Test
    public void shouldForwardPostBodyToServerB() {
        serverB = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/api/multiply", Method.POST, state, new SimpleMultiplyHandler()),
                builder -> builder.setPort(SERVER_B_PORT));

        final LuxisHttpClient httpClient = createHttpClient(mode, serverB, SERVER_B_HOST, SERVER_B_PORT);

        serverA = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/forward", Method.POST, state, new HttpClientPostCallHandler(httpClient, "127.0.0.1:" + SERVER_B_PORT)));

        final String bodyForB = json().put("value", 7).toString();
        final TestHttpResponse response = serverA.client().post(
                StubRequest.request("/forward")
                        .body(json()
                                .put("targetPath", "/api/multiply")
                                .put("forwardBody", bodyForB)
                                .toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json()
                        .put("statusCode", 200)
                        .put("body", json().put("result", 70).toString())
                        .toString()),
                response);
    }

    @Test
    public void shouldHandleServerBReturningError() {
        serverB = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/api/error", Method.GET, state,
                        new ErrorHandler(ErrorStatusCode.BAD_REQUEST, "bad input")),
                builder -> builder.setPort(SERVER_B_PORT));

        final LuxisHttpClient httpClient = createHttpClient(mode, serverB, SERVER_B_HOST, SERVER_B_PORT);

        serverA = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/call-error", Method.POST, state, new HttpClientCallHandler(httpClient, SERVER_B_BASE_URL)));

        final TestHttpResponse response = serverA.client().post(
                StubRequest.request("/call-error")
                        .body(json().put("targetPath", "/api/error").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json()
                        .put("statusCode", 400)
                        .put("body", json()
                                .put("message", "bad input")
                                .set("errors", json())
                                .toString())
                        .toString()),
                response);
    }

    @Test
    public void shouldForwardPutRequestToServerB() {
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/multiply", Method.PUT, state, new SimpleMultiplyHandler());
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/put-forward", Method.POST, state, new HttpClientPutCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final String bodyForB = json().put("value", 5).toString();
        final TestHttpResponse response = client.post(
            StubRequest.request("/put-forward")
                .body(json()
                    .put("targetPath", "/api/multiply")
                    .put("forwardBody", bodyForB)
                    .toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("statusCode", 200)
                .put("body", json().put("result", 50).toString())
                .toString()),
            response);
    }

    @Test
    public void shouldForwardDeleteRequestToServerB() {
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/resource", Method.DELETE, state, new SimpleGetHandler(99));
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/delete-forward", Method.POST, state, new HttpClientDeleteCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final TestHttpResponse response = client.post(
            StubRequest.request("/delete-forward")
                .body(json().put("targetPath", "/api/resource").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("statusCode", 200)
                .put("body", json().put("result", 99).toString())
                .toString()),
            response);
    }

    @Test
    public void shouldPropagateInternalServerErrorFromServerB() {
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/broken", Method.GET, state,
                new ErrorHandler(ErrorStatusCode.INTERNAL_SERVER_ERROR, "something went wrong"));
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/call-broken", Method.POST, state, new HttpClientCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final TestHttpResponse response = client.post(
            StubRequest.request("/call-broken")
                .body(json().put("targetPath", "/api/broken").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("statusCode", 500)
                .put("body", json()
                    .put("message", "something went wrong")
                    .set("errors", json())
                    .toString())
                .toString()),
            response);
    }

    @Test
    public void shouldPropagateUnauthorizedErrorFromServerB() {
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/protected", Method.GET, state,
                new ErrorHandler(ErrorStatusCode.UNAUTHORIZED, "authentication required"));
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/call-protected", Method.POST, state, new HttpClientCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final TestHttpResponse response = client.post(
            StubRequest.request("/call-protected")
                .body(json().put("targetPath", "/api/protected").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("statusCode", 401)
                .put("body", json()
                    .put("message", "authentication required")
                    .set("errors", json())
                    .toString())
                .toString()),
            response);
    }

    @Test
    public void shouldPropagateNotFoundErrorFromServerB() {
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/item", Method.GET, state,
                new ErrorHandler(ErrorStatusCode.NOT_FOUND, "item not found"));
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/call-item", Method.POST, state, new HttpClientCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final TestHttpResponse response = client.post(
            StubRequest.request("/call-item")
                .body(json().put("targetPath", "/api/item").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("statusCode", 404)
                .put("body", json()
                    .put("message", "item not found")
                    .set("errors", json())
                    .toString())
                .toString()),
            response);
    }

    @Test
    public void shouldChainCallsToServerBSequentially() {
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/seed", Method.GET, state, new SimpleGetHandler(6));
            r.jsonRoute("/api/multiply", Method.POST, state, new SimpleMultiplyHandler());
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/chain", Method.POST, state,
                new HttpClientChainedCallHandler(httpClient, "/api/seed", "/api/multiply"));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final TestHttpResponse response = client.post(
            StubRequest.request("/chain").body("{}"));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("statusCode", 200)
                .put("body", json().put("result", 60).toString())
                .toString()),
            response);
    }
}
