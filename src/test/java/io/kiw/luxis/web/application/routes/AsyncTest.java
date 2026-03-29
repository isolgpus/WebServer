package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestClient;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.test.handler.AsyncBlockingMapTestHandler;
import io.kiw.luxis.web.test.handler.AsyncCustomTimeoutTestHandler;
import io.kiw.luxis.web.test.handler.AsyncMapTestHandler;
import io.kiw.luxis.web.test.handler.AsyncThrowTestHandler;
import io.kiw.luxis.web.test.handler.AsyncTimeoutTestHandler;
import io.kiw.luxis.web.test.handler.AsyncWithHttpContextTestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.*;
import static io.kiw.luxis.web.test.TestHelper.json;

@RunWith(Parameterized.class)
public class AsyncTest {


    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestClientAndServer testClientAndServer;

    public AsyncTest(final String mode) {
        this.mode = mode;
    }

    @Before
    public void setUp() {
        if (REAL_MODE.equals(mode)) {
            assumeRealModeEnabled();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (testClientAndServer != null) {
            testClientAndServer.client().assertNoMoreExceptions();
            testClientAndServer.close();
        }
    }

    @Test
    public void shouldSupportCorrelatedAsyncMap() {
        final AsyncMapTestHandler handler = new AsyncMapTestHandler();

        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/async", Method.POST, state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient luxisTestClient = testClientAndServer.client();

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/async").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportCorrelatedAsyncBlockingMap() {
        AsyncBlockingMapTestHandler handler = new AsyncBlockingMapTestHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/asyncBlocking", Method.POST, state, handler);
        });
        TestClient luxisTestClient = testClientAndServer.client();
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/asyncBlocking").body(json().put("value", 3).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldReturnErrorWhenAsyncResponseIsError() {
        AsyncMapTestHandler handler = new AsyncMapTestHandler(value -> HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async error")));

        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/async", Method.POST, state,
                handler);
        });
        TestClient luxisTestClient = testClientAndServer.client();
        handler.evillyReferenceLuxis(testClientAndServer.luxis());

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/async").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("message", "async error").set("errors", json()).toString()).withStatusCode(400),
                response);
    }

    @Test
    public void shouldPassInputValueToHandler() {
        AsyncMapTestHandler handler = new AsyncMapTestHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/async", Method.POST, state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/async").body(json().put("value", 42).toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("result", 420).toString()).withStatusCode(200),
            response);
    }


    @Test
    public void shouldHandleExceptionInCorrelatedAsyncHandler() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new AsyncThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/throw").body(json().put("value", 1).toString()));

        Assert.assertEquals(500, response.statusCode);
        luxisTestClient.assertException("app error in asyncMap");
    }

    @Test
    public void shouldWorkWithPipelineStepsBeforeCorrelatedAsync() {
        AsyncWithHttpContextTestHandler vertxJsonRoute = new AsyncWithHttpContextTestHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/withContext", Method.POST, state, vertxJsonRoute);
        });
        TestClient luxisTestClient = testClientAndServer.client();

        vertxJsonRoute.evillyReferenceLuxis(testClientAndServer.luxis());

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/withContext")
                        .body(json().put("value", 7).toString())
                        .queryParam("multiplier", "3"));

        // multiplier=3, handler responds with 3 * 7 = 21
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 21).toString()),
                response);
    }

    @Test
    public void shouldReturnDifferentErrorStatusCodes() {
        AsyncMapTestHandler handler = new AsyncMapTestHandler(value -> HttpResult.error(ErrorStatusCode.NOT_FOUND, new ErrorMessageResponse("not found")));
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/async", Method.POST, state,
                handler);
        });
        TestClient luxisTestClient = testClientAndServer.client();
        handler.evillyReferenceLuxis(testClientAndServer.luxis());

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/async").body(json().put("value", 1).toString()));

        Assert.assertEquals(404, response.statusCode);
    }

    @Test
    public void shouldTimeoutWithCustomOneSecondTimeout() {
        final AsyncCustomTimeoutTestHandler handler = new AsyncCustomTimeoutTestHandler();

        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/customTimeout", Method.POST, state, handler);
        });

        if (STUB_MODE.equals(mode)) {
            handler.setOnRegistered(() -> ((TestLuxis<?>) testClientAndServer.luxis()).advanceTimeBy(1_001));
        }

        final TestClient luxisTestClient = testClientAndServer.client();

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/customTimeout").body(json().put("value", 1).toString()));

        Assert.assertEquals(500, response.statusCode);
        luxisTestClient.assertException("Correlated async response timed out");
    }
}
