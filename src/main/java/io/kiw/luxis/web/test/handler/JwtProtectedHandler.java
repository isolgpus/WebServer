package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.EmptyRequest;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class JwtProtectedHandler extends VertxJsonRoute<EmptyRequest, SubjectResponse, MyApplicationState> {

    private final JwtProvider jwtProvider;

    public JwtProtectedHandler(final JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public RequestPipeline<SubjectResponse> handle(HttpStream<EmptyRequest, MyApplicationState> stream) {
        return stream
            .requireJwt(jwtProvider)
            .complete(ctx ->
                HttpResult.success(new SubjectResponse(ctx.http().getJwtClaims().getSubject())));
    }
}
