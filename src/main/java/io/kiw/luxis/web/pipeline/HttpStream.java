package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpContext;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.LuxisMapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RouteContext;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.jwt.JwtClaims;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.validation.HttpValidator;

import java.util.List;
import java.util.function.Consumer;

public class HttpStream<IN, APP> extends HttpMapStream<IN, APP> {

    public HttpStream(final List<LuxisMapInstruction<HttpErrorResponse>> instructionChain, final boolean canFinishSuccessfully, final APP applicationState, final Ender ender, final PendingAsyncResponses pendingAsyncResponses) {
        super(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    @SuppressWarnings("unchecked")
    public HttpStream<IN, APP> validate(final Consumer<HttpValidator<IN>> config) {
        addSyncInstruction(false, (state, transport, app) -> {
            final HttpValidator<IN> v = new HttpValidator<>((IN) state, (HttpContext) transport, "");
            config.accept(v);
            return v.toResult();
        }, false);
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    @SuppressWarnings("unchecked")
    public HttpStream<IN, APP> requireJwt(final JwtProvider jwtProvider) {
        addSyncInstruction(false, (state, transport, app) -> {
            final HttpContext httpContext = (HttpContext) transport;
            final String authHeader = httpContext.getRequestHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse("Missing or invalid Authorization header"));
            }
            final String token = authHeader.substring(7);
            final Result<String, JwtClaims> result = jwtProvider.validateToken(token);
            return result.fold(
                    error -> HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse(error)),
                    claims -> {
                        httpContext.ctx.put("__jwt_claims__", claims);
                        return HttpResult.success((IN) state);
                    }
            );
        }, false);
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }
}
