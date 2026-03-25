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
import io.kiw.luxis.web.test.handler.CorrelatedAsyncMapTestHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static io.kiw.luxis.web.test.TestHelper.json;

public class CorrelatedAsyncTest {

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

        // Poll until the handler has run and registered the correlation ID
        long correlationId;
        while (true) {
            final long[] holder = {-1};
            luxis.apply(null, (ignored, app) -> holder[0] = app.getPendingCorrelationId());
            if (holder[0] != -1) {
                correlationId = holder[0];
                break;
            }
            Thread.sleep(10);
        }

        // Complete the async response externally
        luxis.handleAsyncResponse(correlationId, Result.success(50));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportCorrelatedAsyncMapWithError() throws Exception {
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

        // Poll until the handler has run and registered the correlation ID
        long correlationId;
        while (true) {
            final long[] holder = {-1};
            luxis.apply(null, (ignored, app) -> holder[0] = app.getPendingCorrelationId());
            if (holder[0] != -1) {
                correlationId = holder[0];
                break;
            }
            Thread.sleep(10);
        }

        // Complete with error
        luxis.handleAsyncResponse(correlationId, HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async error")));

        final TestHttpResponse response = responseFuture.join();
        Assert.assertEquals(400, response.statusCode);
    }
}
