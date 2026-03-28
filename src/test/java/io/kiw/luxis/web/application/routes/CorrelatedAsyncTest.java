package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestClient;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.application.routes.TestClientAndServer;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncBlockingMapTestHandler;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncMapTestHandler;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncThrowTestHandler;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncWithHttpContextTestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.*;
import static io.kiw.luxis.web.test.TestHelper.json;

@RunWith(Parameterized.class)
public class CorrelatedAsyncTest {


    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestClientAndServer testClientAndServer;

    public CorrelatedAsyncTest(final String mode) {
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
        final CorrelatedAsyncMapTestHandler handler = new CorrelatedAsyncMapTestHandler();

        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient luxisTestClient = testClientAndServer.client();

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportCorrelatedAsyncBlockingMap() {
        CorrelatedAsyncBlockingMapTestHandler handler = new CorrelatedAsyncBlockingMapTestHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsyncBlocking", Method.POST, state, handler);
        });
        TestClient luxisTestClient = testClientAndServer.client();
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsyncBlocking").body(json().put("value", 3).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldReturnErrorWhenAsyncResponseIsError() {
        CorrelatedAsyncMapTestHandler handler = new CorrelatedAsyncMapTestHandler(value -> HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async error")));

        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state,
                handler);
        });
        TestClient luxisTestClient = testClientAndServer.client();
        handler.evillyReferenceLuxis(testClientAndServer.luxis());

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("message", "async error").set("errors", json()).toString()).withStatusCode(400),
                response);
    }

    @Test
    public void shouldPassInputValueToHandler() {
        CorrelatedAsyncMapTestHandler handler = new CorrelatedAsyncMapTestHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/correlatedAsync").body(json().put("value", 42).toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("result", 420).toString()).withStatusCode(200),
            response);
    }


    @Test
    public void shouldHandleExceptionInCorrelatedAsyncHandler() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new CorrelatedAsyncThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/throw").body(json().put("value", 1).toString()));

        Assert.assertEquals(500, response.statusCode);
        luxisTestClient.assertException("app error in correlatedAsyncMap");
    }

    @Test
    public void shouldWorkWithPipelineStepsBeforeCorrelatedAsync() {
        CorrelatedAsyncWithHttpContextTestHandler vertxJsonRoute = new CorrelatedAsyncWithHttpContextTestHandler();
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
        CorrelatedAsyncMapTestHandler handler = new CorrelatedAsyncMapTestHandler(value -> HttpResult.error(ErrorStatusCode.NOT_FOUND, new ErrorMessageResponse("not found")));
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state,
                handler);
        });
        TestClient luxisTestClient = testClientAndServer.client();
        handler.evillyReferenceLuxis(testClientAndServer.luxis());

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 1).toString()));

        Assert.assertEquals(404, response.statusCode);
    }
}
