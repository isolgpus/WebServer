package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.jwt.JwtClaims;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.validation.Validator;

import java.util.List;
import java.util.function.Consumer;

public class HttpStream<IN, APP> {
    private final List<MapInstruction> instructionChain;
    private final boolean canFinishSuccessfully;
    private final APP applicationState;
    private final Ender ender;

    public HttpStream(final List<MapInstruction> instructionChain, final boolean canFinishSuccessfully, final APP applicationState, final Ender ender) {
        this.instructionChain = instructionChain;
        this.canFinishSuccessfully = canFinishSuccessfully;
        this.applicationState = applicationState;
        this.ender = ender;
    }

    public <OUT> HttpStream<OUT, APP> map(final HttpControlStreamMapper<IN, OUT, APP> flowHandler) {

        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpStream<OUT, APP> flatMap(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }


    public <OUT> HttpStream<OUT, APP> blockingMap(final HttpControlStreamBlockingMapper<IN, OUT> flowHandler) {

        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public HttpStream<IN, APP> validate(final Consumer<Validator<IN>> config) {
        return flatMap(ctx -> {
            final Validator<IN> v = new Validator<>(ctx.in(), ctx.http(), "");
            config.accept(v);
            return v.toResult();
        });
    }

    public HttpStream<IN, APP> requireJwt(final JwtProvider jwtProvider) {
        return flatMap(ctx -> {
            final String authHeader = ctx.http().getRequestHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse("Missing or invalid Authorization header"));
            }
            final String token = authHeader.substring(7);
            final Result<String, JwtClaims> result = jwtProvider.validateToken(token);
            return result.fold(
                error -> HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse(error)),
                claims -> {
                    ctx.http().ctx.put("__jwt_claims__", claims);
                    return HttpResult.success(ctx.in());
                }
            );
        });
    }

    public <OUT> HttpStream<OUT, APP> blockingFlatMap(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> HttpStream<OUT, APP> asyncMap(final HttpControlStreamAsyncMapper<IN, OUT, APP> flowHandler) {
        return asyncFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> HttpStream<OUT, APP> asyncFlatMap(final HttpControlStreamAsyncFlatMapper<IN, OUT, APP> mapper) {
        instructionChain.add(new MapInstruction<>(mapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> HttpStream<OUT, APP> asyncBlockingMap(final HttpControlStreamAsyncBlockingMapper<IN, OUT> flowHandler) {
        return asyncBlockingFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> HttpStream<OUT, APP> asyncBlockingFlatMap(final HttpControlStreamAsyncBlockingFlatMapper<IN, OUT> mapper) {
        instructionChain.add(new MapInstruction<>(mapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> RequestPipeline<OUT> complete(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }

    public <OUT> RequestPipeline<OUT> complete() {
        return complete(a -> HttpResult.success());
    }


    public <OUT> RequestPipeline<OUT>  blockingComplete(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }
}
