package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestClient;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

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

    private long waitForCorrelationId(final MyApplicationState state) throws InterruptedException {
        while (true) {
            final long id = state.getPendingCorrelationId();
            if (id != -1) {
                return id;
            }
            Thread.sleep(10);
        }
    }

    @Test
    public void shouldSupportCorrelatedAsyncMap() throws Exception {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()))
        );

        final long correlationId = waitForCorrelationId(stateRef[0]);
        Assert.assertEquals(0L, correlationId);

        luxisTestClient.handleAsyncResponse(correlationId, Result.success(50));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportCorrelatedAsyncBlockingMap() throws Exception {
        final AtomicLong capturedCorrelationId = new AtomicLong(-1);
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsyncBlocking", Method.POST, state, new CorrelatedAsyncBlockingMapTestHandler(capturedCorrelationId));
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/correlatedAsyncBlocking").body(json().put("value", 3).toString()))
        );

        long correlationId;
        while (true) {
            correlationId = capturedCorrelationId.get();
            if (correlationId != -1) {
                break;
            }
            Thread.sleep(10);
        }
        Assert.assertEquals(0L, correlationId);

        luxisTestClient.handleAsyncResponse(correlationId, Result.success(60));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldReturnErrorWhenAsyncResponseIsError() throws Exception {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()))
        );

        final long correlationId = waitForCorrelationId(stateRef[0]);

        luxisTestClient.handleAsyncResponse(correlationId, HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async error")));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(400, response.statusCode);
        Assert.assertEquals(
                TestHttpResponse.response(json().put("message", "async error").set("errors", json()).toString()).withStatusCode(400),
                response);
    }

    @Test
    public void shouldPassInputValueToHandler() throws Exception {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 42).toString()))
        );

        waitForCorrelationId(stateRef[0]);

        Assert.assertEquals(42, stateRef[0].getPendingValue());

        luxisTestClient.handleAsyncResponse(0L, Result.success(100));
        responseFuture.join();
    }

    @Test
    public void shouldAssignIncrementingCorrelationIds() throws Exception {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        // First request
        final CompletableFuture<TestHttpResponse> response1 = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 1).toString()))
        );
        final long id1 = waitForCorrelationId(stateRef[0]);
        Assert.assertEquals(0L, id1);
        luxisTestClient.handleAsyncResponse(id1, Result.success(10));
        response1.join();

        // Reset state for next request
        stateRef[0].setPendingCorrelationId(-1);

        // Second request should get next correlation ID
        final CompletableFuture<TestHttpResponse> response2 = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 2).toString()))
        );
        final long id2 = waitForCorrelationId(stateRef[0]);
        Assert.assertEquals(1L, id2);
        luxisTestClient.handleAsyncResponse(id2, Result.success(20));
        response2.join();
    }

    @Test
    public void shouldHandleExceptionInCorrelatedAsyncHandler() {
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new CorrelatedAsyncThrowTestHandler());
        });

        // The handler throws synchronously inside the async wrapper,
        // which is caught by handleAsyncBlocking and returns a 500
        final TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/throw").body(json().put("value", 1).toString()));

        Assert.assertEquals(500, response.statusCode);
        luxisTestClient.assertException("app error in correlatedAsyncMap");
    }

    @Test
    public void shouldWorkWithPipelineStepsBeforeCorrelatedAsync() throws Exception {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/withContext", Method.POST, state, new CorrelatedAsyncWithHttpContextTestHandler());
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/withContext")
                                .body(json().put("value", 7).toString())
                                .queryParam("multiplier", "3"))
        );

        final long correlationId = waitForCorrelationId(stateRef[0]);

        Assert.assertEquals(3, stateRef[0].getPendingValue());

        luxisTestClient.handleAsyncResponse(correlationId, Result.success(21));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 21).toString()),
                response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenCompletingUnknownCorrelationId() {
        luxisTestClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });
        luxisTestClient.handleAsyncResponse(999L, Result.success("value"));
    }

    @Test
    public void shouldReturnDifferentErrorStatusCodes() throws Exception {
        final MyApplicationState[] stateRef = new MyApplicationState[1];
        luxisTestClient = createClient(mode, (r, state) -> {
            stateRef[0] = state;
            r.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxisTestClient.post(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 1).toString()))
        );

        final long correlationId = waitForCorrelationId(stateRef[0]);

        luxisTestClient.handleAsyncResponse(correlationId, HttpResult.error(ErrorStatusCode.NOT_FOUND, new ErrorMessageResponse("not found")));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(404, response.statusCode);
    }
}
