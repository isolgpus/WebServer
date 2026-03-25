package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestClient;
import io.kiw.luxis.web.test.MyApplicationState;
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
    private TestClient luxisTestClient;

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
        if (luxisTestClient != null) {
            luxisTestClient.assertNoMoreExceptions();
            luxisTestClient.close();
        }
    }

    @Test
    public void shouldSupportCorrelatedAsyncMap() {
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportCorrelatedAsyncBlockingMap() {
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsyncBlocking", Method.POST, state, new CorrelatedAsyncBlockingMapTestHandler(state));
        });

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsyncBlocking").body(json().put("value", 3).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldReturnErrorWhenAsyncResponseIsError() {
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state,
                    new CorrelatedAsyncMapTestHandler(value -> HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async error"))));
        });

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()));

        Assert.assertEquals(400, response.statusCode);
        Assert.assertEquals(
                TestHttpResponse.response(json().put("message", "async error").set("errors", json()).toString()).withStatusCode(400),
                response);
    }

    @Test
    public void shouldPassInputValueToHandler() {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 42).toString()));

        Assert.assertEquals(42, stateRef[0].getPendingValue());
    }

    @Test
    public void shouldAssignIncrementingCorrelationIds() {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 1).toString()));
        Assert.assertEquals(0L, stateRef[0].getPendingCorrelationId());

        stateRef[0].setPendingCorrelationId(-1);

        luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 2).toString()));
        Assert.assertEquals(1L, stateRef[0].getPendingCorrelationId());
    }

    @Test
    public void shouldHandleExceptionInCorrelatedAsyncHandler() {
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new CorrelatedAsyncThrowTestHandler());
        });

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/throw").body(json().put("value", 1).toString()));

        Assert.assertEquals(500, response.statusCode);
        luxisTestClient.assertException("app error in correlatedAsyncMap");
    }

    @Test
    public void shouldWorkWithPipelineStepsBeforeCorrelatedAsync() {
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/withContext", Method.POST, state, new CorrelatedAsyncWithHttpContextTestHandler());
        });

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
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state,
                    new CorrelatedAsyncMapTestHandler(value -> HttpResult.error(ErrorStatusCode.NOT_FOUND, new ErrorMessageResponse("not found"))));
        });

        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/correlatedAsync").body(json().put("value", 1).toString()));

        Assert.assertEquals(404, response.statusCode);
    }
}
