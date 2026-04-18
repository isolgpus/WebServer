package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.HttpSession;
import io.kiw.luxis.web.internal.LuxisPipeline;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RestrictedBlockingRouteContext;
import io.kiw.luxis.web.internal.RouteContext;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.jwt.JwtClaims;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.validation.HttpValidator;

import java.util.List;
import java.util.function.Consumer;

public class HttpStream<IN, APP> extends LuxisStream<IN, APP, Void, HttpErrorResponse, HttpSession> {

    public HttpStream(final List<MapInstruction> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses, final Ender ender) {
        super(instructionChain, applicationState, pendingAsyncResponses, httpErr -> httpErr, ender);
    }

    public HttpStream<IN, APP> validate(final Consumer<HttpValidator<IN>> config) {
        appendValidation(ctx -> {
            final HttpValidator<IN> v = new HttpValidator<>(ctx.in(), ctx.session(), "");
            config.accept(v);
            return v.toHttpResult();
        });
        return new HttpStream<>(instructionChain, applicationState, pendingAsyncResponses, ender);
    }

    public HttpStream<IN, APP> requireJwt(final JwtProvider jwtProvider) {
        final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, IN> mapper = ctx -> {
            final String authHeader = ctx.session().getRequestHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse("Missing or invalid Authorization header"));
            }
            final String token = authHeader.substring(7);
            final Result<String, JwtClaims> result = jwtProvider.validateToken(token);
            return result.fold(
                    error -> HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse(error)),
                    claims -> {
                        ctx.session().ctx.put("__jwt_claims__", claims);
                        return HttpResult.success(ctx.in());
                    }
            );
        };
        final MapInstruction<IN, IN, APP, HttpSession, HttpErrorResponse> e = MapInstruction.nonBlocking(mapper, false);
        appendInstruction(e);
        return new HttpStream<>(instructionChain, applicationState, pendingAsyncResponses, ender);
    }

    @Override
    public <OUT> LuxisPipeline<OUT> complete(final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> mapper) {
        final MapInstruction<IN, OUT, APP, HttpSession, HttpErrorResponse> e = MapInstruction.nonBlocking(mapper, ender != null);
        appendInstruction(e);
        return new LuxisPipeline<>(instructionChain, applicationState, ender);
    }

    @Override
    public LuxisPipeline<IN> complete() {
        return complete(ctx -> Result.success(ctx.in()));
    }

    @Override
    public <OUT> LuxisPipeline<OUT> blockingComplete(final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, HttpErrorResponse, OUT> mapper) {
        final MapInstruction<IN, OUT, Object, HttpSession, HttpErrorResponse> e =
                MapInstruction.blocking(mapper, (in, session) -> new RestrictedBlockingRouteContext<>(in), ender != null);
        appendInstruction(e);
        return new LuxisPipeline<>(instructionChain, applicationState, ender);
    }
}
