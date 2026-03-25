package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.jwt.JwtClaims;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.validation.HttpValidator;

import java.util.List;
import java.util.function.Consumer;

public class HttpStream<IN, APP> extends HttpMapStream<IN, APP> {

    public HttpStream(final List<MapInstruction> instructionChain, final boolean canFinishSuccessfully, final APP applicationState, final Ender ender, final PendingAsyncResponses pendingAsyncResponses) {
        super(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public HttpStream<IN, APP> validate(final Consumer<HttpValidator<IN>> config) {
        instructionChain.add(new MapInstruction<>(false, (HttpControlStreamFlatMapper<IN, IN, APP>) ctx -> {
            final HttpValidator<IN> v = new HttpValidator<>(ctx.in(), ctx.http(), "");
            config.accept(v);
            return v.toResult();
        }, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public HttpStream<IN, APP> requireJwt(final JwtProvider jwtProvider) {
        instructionChain.add(new MapInstruction<>(false, (HttpControlStreamFlatMapper<IN, IN, APP>) ctx -> {
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
        }, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }
}
