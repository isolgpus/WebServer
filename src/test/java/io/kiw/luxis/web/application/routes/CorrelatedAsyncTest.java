package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncBlockingMapTestHandler;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncMapTestHandler;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncThrowTestHandler;
import io.kiw.luxis.web.test.handler.CorrelatedAsyncWithHttpContextTestHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.kiw.luxis.web.test.TestHelper.json;

public class CorrelatedAsyncTest {

    private long waitForCorrelationId(final TestLuxis<MyApplicationState> luxis) throws InterruptedException {
        while (true) {
            final long[] holder = {-1};
            luxis.apply(null, (ignored, app) -> holder[0] = app.getPendingCorrelationId());
            if (holder[0] != -1) {
                return holder[0];
            }
            Thread.sleep(10);
        }
    }

    @Test
    public void shouldSupportCorrelatedAsyncMap() throws Exception {
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
            return state;
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()),
                        Method.POST)
        );

        final long correlationId = waitForCorrelationId(luxis);
        Assert.assertEquals(0L, correlationId);

        luxis.handleAsyncResponse(correlationId, Result.success(50));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportCorrelatedAsyncBlockingMap() throws Exception {
        CorrelatedAsyncBlockingMapTestHandler.capturedCorrelationId.set(-1);
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/correlatedAsyncBlocking", Method.POST, state, new CorrelatedAsyncBlockingMapTestHandler());
            return state;
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/correlatedAsyncBlocking").body(json().put("value", 3).toString()),
                        Method.POST)
        );

        // Poll static field since blocking context doesn't have app state
        long correlationId;
        while (true) {
            correlationId = CorrelatedAsyncBlockingMapTestHandler.capturedCorrelationId.get();
            if (correlationId != -1) {
                break;
            }
            Thread.sleep(10);
        }
        Assert.assertEquals(0L, correlationId);

        luxis.handleAsyncResponse(correlationId, Result.success(60));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldReturnErrorWhenAsyncResponseIsError() throws Exception {
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
            return state;
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 5).toString()),
                        Method.POST)
        );

        final long correlationId = waitForCorrelationId(luxis);

        luxis.handleAsyncResponse(correlationId, HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async error")));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(400, response.statusCode);
        Assert.assertEquals(
                TestHttpResponse.response(json().put("message", "async error").set("errors", json()).toString()).withStatusCode(400),
                response);
    }

    @Test
    public void shouldPassInputValueToHandler() throws Exception {
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
            return state;
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 42).toString()),
                        Method.POST)
        );

        waitForCorrelationId(luxis);

        // Verify the handler received the input value
        final int[] capturedValue = {0};
        luxis.apply(null, (ignored, app) -> capturedValue[0] = app.getPendingValue());
        Assert.assertEquals(42, capturedValue[0]);

        luxis.handleAsyncResponse(0L, Result.success(100));
        responseFuture.join();
    }

    @Test
    public void shouldAssignIncrementingCorrelationIds() throws Exception {
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
            return state;
        });

        // First request
        final CompletableFuture<TestHttpResponse> response1 = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 1).toString()),
                        Method.POST)
        );
        final long id1 = waitForCorrelationId(luxis);
        Assert.assertEquals(0L, id1);
        luxis.handleAsyncResponse(id1, Result.success(10));
        response1.join();

        // Reset state for next request
        luxis.apply(null, (ignored, app) -> app.setPendingCorrelationId(-1));

        // Second request should get next correlation ID
        final CompletableFuture<TestHttpResponse> response2 = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 2).toString()),
                        Method.POST)
        );
        final long id2 = waitForCorrelationId(luxis);
        Assert.assertEquals(1L, id2);
        luxis.handleAsyncResponse(id2, Result.success(20));
        response2.join();
    }

    @Test
    public void shouldHandleExceptionInCorrelatedAsyncHandler() {
        final List<Exception> exceptions = new ArrayList<>();
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/throw", Method.POST, state, new CorrelatedAsyncThrowTestHandler());
            return state;
        });
        luxis.setExceptionHandler(exceptions::add);

        // The handler throws synchronously inside the async wrapper,
        // which is caught by handleAsyncBlocking and returns a 500
        final TestHttpResponse response = luxis.getRouter().handle(
                StubRequest.request("/throw").body(json().put("value", 1).toString()),
                Method.POST);

        Assert.assertEquals(500, response.statusCode);
        Assert.assertEquals(1, exceptions.size());
        Assert.assertTrue(exceptions.get(0).getMessage().contains("app error in correlatedAsyncMap"));
    }

    @Test
    public void shouldWorkWithPipelineStepsBeforeCorrelatedAsync() throws Exception {
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/withContext", Method.POST, state, new CorrelatedAsyncWithHttpContextTestHandler());
            return state;
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/withContext")
                                .body(json().put("value", 7).toString())
                                .queryParam("multiplier", "3"),
                        Method.POST)
        );

        final long correlationId = waitForCorrelationId(luxis);

        // Verify the map step before correlatedAsyncMap ran and passed its result
        final int[] capturedValue = {0};
        luxis.apply(null, (ignored, app) -> capturedValue[0] = app.getPendingValue());
        Assert.assertEquals(3, capturedValue[0]);

        luxis.handleAsyncResponse(correlationId, Result.success(21));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 21).toString()),
                response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenCompletingUnknownCorrelationId() {
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
            return state;
        });
        luxis.handleAsyncResponse(999L, Result.success("value"));
    }

    @Test
    public void shouldReturnDifferentErrorStatusCodes() throws Exception {
        final TestLuxis<MyApplicationState> luxis = Luxis.test(routesRegister -> {
            final MyApplicationState state = new MyApplicationState();
            routesRegister.jsonRoute("/correlatedAsync", Method.POST, state, new CorrelatedAsyncMapTestHandler());
            return state;
        });

        final CompletableFuture<TestHttpResponse> responseFuture = CompletableFuture.supplyAsync(() ->
                luxis.getRouter().handle(
                        StubRequest.request("/correlatedAsync").body(json().put("value", 1).toString()),
                        Method.POST)
        );

        final long correlationId = waitForCorrelationId(luxis);

        luxis.handleAsyncResponse(correlationId, HttpResult.error(ErrorStatusCode.NOT_FOUND, new ErrorMessageResponse("not found")));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(404, response.statusCode);
    }
}
