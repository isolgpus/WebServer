package io.kiw.web.infrastructure;

import io.kiw.result.Result;
import io.kiw.web.infrastructure.ender.Ender;
import io.kiw.web.infrastructure.jwt.JwtClaims;
import io.kiw.web.infrastructure.jwt.JwtProvider;

import java.util.List;
import java.util.function.Consumer;

public class HttpStream<IN, APP> {
    private final List<MapInstruction> instructionChain;
    private final boolean canFinishSuccessfully;
    private final APP applicationState;
    private final Ender ender;

    public HttpStream(List<MapInstruction> instructionChain, boolean canFinishSuccessfully, APP applicationState, Ender ender) {
        this.instructionChain = instructionChain;
        this.canFinishSuccessfully = canFinishSuccessfully;
        this.applicationState = applicationState;
        this.ender = ender;
    }

    public <OUT> HttpStream<OUT, APP> map(HttpControlStreamMapper<IN, OUT, APP> flowHandler)
    {

        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpStream<OUT, APP> flatMap(HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }


    public <OUT> HttpStream<OUT, APP> blockingMap(HttpControlStreamBlockingMapper<IN, OUT> flowHandler)
    {

        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public HttpStream<IN, APP> validate(Consumer<Validator<IN>> config) {
        return flatMap(ctx -> {
            Validator<IN> v = new Validator<>(ctx.in(), ctx.http(), "");
            config.accept(v);
            return v.toResult();
        });
    }

    public HttpStream<IN, APP> requireJwt(JwtProvider jwtProvider) {
        return flatMap(ctx -> {
            String authHeader = ctx.http().getRequestHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse("Missing or invalid Authorization header"));
            }
            String token = authHeader.substring(7);
            Result<String, JwtClaims> result = jwtProvider.validateToken(token);
            return result.fold(
                error -> HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse(error)),
                claims -> {
                    ctx.http().ctx.put("__jwt_claims__", claims);
                    return HttpResult.success(ctx.in());
                }
            );
        });
    }

    public <OUT> HttpStream<OUT, APP> blockingFlatMap(HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> HttpStream<OUT, APP> asyncMap(HttpControlStreamAsyncMapper<IN, OUT, APP> flowHandler)
    {
        return asyncFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> HttpStream<OUT, APP> asyncFlatMap(HttpControlStreamAsyncFlatMapper<IN, OUT, APP> mapper)
    {
        instructionChain.add(new MapInstruction<>(mapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> HttpStream<OUT, APP> asyncBlockingMap(HttpControlStreamAsyncBlockingMapper<IN, OUT> flowHandler)
    {
        return asyncBlockingFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> HttpStream<OUT, APP> asyncBlockingFlatMap(HttpControlStreamAsyncBlockingFlatMapper<IN, OUT> mapper)
    {
        instructionChain.add(new MapInstruction<>(mapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> RequestPipeline<OUT> complete(HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }

    public <OUT> RequestPipeline<OUT>  blockingComplete(HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }
}
