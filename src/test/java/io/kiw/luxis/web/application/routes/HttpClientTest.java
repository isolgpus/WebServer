package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.http.client.LuxisHttpClient;
import io.kiw.luxis.web.http.client.LuxisHttpClientConfig;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.test.handler.ErrorHandler;
import io.kiw.luxis.web.test.handler.HttpClientCallHandler;
import io.kiw.luxis.web.test.handler.HttpClientPostCallHandler;
import io.kiw.luxis.web.test.handler.HttpClientTypedGetHandler;
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

        final LuxisHttpClient httpClient = createHttpClient(mode, serverB);

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

        final LuxisHttpClient httpClient = createHttpClient(mode, serverB);

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
    public void shouldCallServerBUsingBaseUrl() {
        serverB = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/api/value", Method.GET, state, new SimpleGetHandler(99)),
                builder -> builder.setPort(SERVER_B_PORT));

        final LuxisHttpClientConfig config = LuxisHttpClientConfig.defaults()
                .baseUrl(SERVER_B_BASE_URL);
        final LuxisHttpClient httpClient = createHttpClient(mode, serverB, config);

        serverA = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/call-b", Method.POST, state, new HttpClientCallHandler(httpClient, "")));

        final TestHttpResponse response = serverA.client().post(
                StubRequest.request("/call-b")
                        .body(json().put("targetPath", "/api/value").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json()
                        .put("statusCode", 200)
                        .put("body", json().put("result", 99).toString())
                        .toString()),
                response);
    }

    @Test
    public void shouldReturnResultErrorWhenErrorAwareAndServerBReturns400() {
        serverB = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/api/error", Method.GET, state,
                        new ErrorHandler(ErrorStatusCode.BAD_REQUEST, "bad input")),
                builder -> builder.setPort(SERVER_B_PORT));

        final LuxisHttpClientConfig config = LuxisHttpClientConfig.defaults()
                .errorAwareResponses(true);
        final LuxisHttpClient httpClient = createHttpClient(mode, serverB, config);

        serverA = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/call-error", Method.POST, state, new HttpClientCallHandler(httpClient, SERVER_B_BASE_URL)));

        final TestHttpResponse response = serverA.client().post(
                StubRequest.request("/call-error")
                        .body(json().put("targetPath", "/api/error").toString()));

        Assert.assertEquals(400, response.statusCode);
        Assert.assertEquals(
                json().put("message", "bad input").set("errors", json()).toString(),
                response.responseBody);
    }

    @Test
    public void shouldDeserializeTypedResponseFromServerB() {
        serverB = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/api/value", Method.GET, state, new SimpleGetHandler(42)),
                builder -> builder.setPort(SERVER_B_PORT));

        final LuxisHttpClientConfig config = LuxisHttpClientConfig.defaults();
        final LuxisHttpClient httpClient = createHttpClient(mode, serverB, config);

        serverA = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/call-b-typed", Method.POST, state, new HttpClientTypedGetHandler(httpClient, SERVER_B_BASE_URL)));

        final TestHttpResponse response = serverA.client().post(
                StubRequest.request("/call-b-typed")
                        .body(json().put("targetPath", "/api/value").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json()
                        .put("result", 42)
                        .toString()),
                response);
    }

    @Test
    public void shouldHandleServerBReturningError() {
        serverB = createTestServerAndClient(mode, (r, state) ->
                r.jsonRoute("/api/error", Method.GET, state,
                        new ErrorHandler(ErrorStatusCode.BAD_REQUEST, "bad input")),
                builder -> builder.setPort(SERVER_B_PORT));

        final LuxisHttpClient httpClient = createHttpClient(mode, serverB);

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
}
