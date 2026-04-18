package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.HttpSession;
import io.kiw.luxis.web.internal.HttpMapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RouteContext;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.jwt.JwtClaims;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.validation.HttpValidator;

import java.util.List;
import java.util.function.Consumer;

public class HttpStream<IN, APP> extends HttpMapStream<IN, APP> {

    public HttpStream(final List<HttpMapInstruction> instructionChain, final boolean canFinishSuccessfully, final APP applicationState, final Ender ender, final PendingAsyncResponses pendingAsyncResponses) {
        super(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public HttpStream<IN, APP> validate(final Consumer<HttpValidator<IN>> config) {
        final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, IN> mapper = ctx -> {
            final HttpValidator<IN> v = new HttpValidator<>(ctx.in(), ctx.session(), "");
            config.accept(v);
            return v.toHttpResult();
        };
        instructionChain.add(HttpMapInstruction.nonBlocking(mapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
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
        instructionChain.add(HttpMapInstruction.nonBlocking(mapper, false));
        return new HttpStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }
}
