package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.http.client.LuxisHttpClient;
import io.kiw.luxis.web.http.client.StubLuxisHttpClient;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.StubTestClient;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.test.handler.ErrorHandler;
import io.kiw.luxis.web.test.handler.HttpClientCallHandler;
import io.kiw.luxis.web.test.handler.HttpClientPostCallHandler;
import io.kiw.luxis.web.test.handler.SimpleGetHandler;
import io.kiw.luxis.web.test.handler.SimpleMultiplyHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static io.kiw.luxis.web.test.TestHelper.json;

public class HttpClientTest {

    private TestLuxis<MyApplicationState> serverA;
    private TestLuxis<MyApplicationState> serverB;

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
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/value", Method.GET, state, new SimpleGetHandler(42));
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/call-b", Method.POST, state, new HttpClientCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final TestHttpResponse response = client.post(
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
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/multiply", Method.POST, state, new SimpleMultiplyHandler());
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/forward", Method.POST, state, new HttpClientPostCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final String bodyForB = json().put("value", 7).toString();
        final TestHttpResponse response = client.post(
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
        serverB = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/api/error", Method.GET, state,
                new ErrorHandler(ErrorStatusCode.BAD_REQUEST, "bad input"));
            return state;
        });

        final LuxisHttpClient httpClient = StubLuxisHttpClient.create(serverB);

        serverA = Luxis.test(r -> {
            final MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/call-error", Method.POST, state, new HttpClientCallHandler(httpClient));
            return state;
        });

        final StubTestClient<MyApplicationState> client = new StubTestClient<>("127.0.0.1", 8080, serverA);

        final TestHttpResponse response = client.post(
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
