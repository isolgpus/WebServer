package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.AsyncMapConfigBuilder;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncRetryTestHandler extends JsonHandler<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    private final AtomicLong counter;

    public AsyncRetryTestHandler(final AtomicLong counter) {
        this.counter = counter;
    }

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .<Integer>asyncMap(ctx -> {
                    counter.incrementAndGet();
                    CompletableFuture<Result<HttpErrorResponse, Integer>> resultCompletableFuture = new CompletableFuture<>();
                    resultCompletableFuture.complete(Result.error(new HttpErrorResponse(new ErrorMessageResponse("Failed running async"), 500)));
                    return new LuxisAsync<>(resultCompletableFuture);
                }, new AsyncMapConfigBuilder().retries(3, 500).build())
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> HttpResult.success(ctx.in()));
    }
}
