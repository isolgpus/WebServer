package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.BlockingAsyncRouteContext;
import io.kiw.luxis.web.http.BlockingRouteContext;
import io.kiw.luxis.web.http.HttpContext;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.AsyncRouteContext;
import io.kiw.luxis.web.internal.LuxisMapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.internal.RouteContext;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.http.HttpResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HttpMapStream<IN, APP> extends LuxisStream<IN, APP, HttpErrorResponse> {
    protected final boolean canFinishSuccessfully;
    protected final Ender ender;

    public HttpMapStream(final List<LuxisMapInstruction<HttpErrorResponse>> instructionChain, final boolean canFinishSuccessfully, final APP applicationState, final Ender ender, final PendingAsyncResponses pendingAsyncResponses) {
        super(instructionChain, applicationState, pendingAsyncResponses);
        this.canFinishSuccessfully = canFinishSuccessfully;
        this.ender = ender;
    }

    @Override
    protected void onInstructionAdded(final LuxisMapInstruction<HttpErrorResponse> instruction) {
        // no-op for HTTP — flat list, no linked-list chaining
    }

    @Override
    protected Function<HttpErrorResponse, HttpErrorResponse> asyncErrorMapper() {
        return Function.identity();
    }

    @Override
    protected <OUT> CompletableFuture<Result<HttpErrorResponse, OUT>> handleAsyncException(final CompletableFuture<Result<HttpErrorResponse, OUT>> future) {
        return future;
    }

    @SuppressWarnings("unchecked")
    public <OUT> HttpMapStream<OUT, APP> map(final HttpControlStreamMapper<IN, OUT, APP> flowHandler) {
        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    @SuppressWarnings("unchecked")
    public <OUT> HttpMapStream<OUT, APP> flatMap(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        addSyncInstruction(false,
                (state, transport, app) -> httpControlStreamFlatMapper.handle(new RouteContext<>((IN) state, (HttpContext) transport, (APP) app)),
                false);
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    @SuppressWarnings("unchecked")
    public HttpMapStream<IN, APP> peek(final HttpControlStreamPeeker<IN, APP> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    @SuppressWarnings("unchecked")
    public HttpMapStream<IN, APP> blockingPeek(final HttpControlStreamBlockingPeeker<IN> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    @SuppressWarnings("unchecked")
    public <OUT> HttpMapStream<OUT, APP> blockingMap(final HttpControlStreamBlockingMapper<IN, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    @SuppressWarnings("unchecked")
    public <OUT> HttpMapStream<OUT, APP> blockingFlatMap(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        addSyncInstruction(true,
                (state, transport, app) -> httpControlStreamFlatMapper.handle(new BlockingRouteContext<>((IN) state, (HttpContext) transport)),
                false);
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final HttpControlStreamAsyncMapper<IN, OUT, APP> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    @SuppressWarnings("unchecked")
    public <OUT> HttpMapStream<OUT, APP> asyncMap(final HttpControlStreamAsyncMapper<IN, OUT, APP> handler, final AsyncMapConfig config) {
        addAsyncMapInstruction(false,
                (state, transport, app, par) -> handler.handle(new AsyncRouteContext<>((IN) state, (HttpContext) transport, (APP) app, par)),
                config);
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final HttpControlStreamAsyncBlockingMapper<IN, OUT> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    @SuppressWarnings("unchecked")
    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final HttpControlStreamAsyncBlockingMapper<IN, OUT> handler, final AsyncMapConfig config) {
        addAsyncMapInstruction(true,
                (state, transport, app, par) -> handler.handle(new BlockingAsyncRouteContext<>((IN) state, (HttpContext) transport, par)),
                config);
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    @SuppressWarnings("unchecked")
    public <OUT> RequestPipeline<OUT> complete(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        addSyncInstruction(false,
                (state, transport, app) -> httpControlStreamFlatMapper.handle(new RouteContext<>((IN) state, (HttpContext) transport, (APP) app)),
                canFinishSuccessfully);
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }

    public RequestPipeline<Void> complete() {
        return complete(a -> HttpResult.success());
    }

    @SuppressWarnings("unchecked")
    public <OUT> RequestPipeline<OUT> blockingComplete(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        addSyncInstruction(true,
                (state, transport, app) -> httpControlStreamFlatMapper.handle(new BlockingRouteContext<>((IN) state, (HttpContext) transport)),
                canFinishSuccessfully);
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }
}
