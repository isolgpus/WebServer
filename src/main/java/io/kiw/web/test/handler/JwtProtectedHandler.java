package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.infrastructure.jwt.JwtProvider;
import io.kiw.web.test.MyApplicationState;

public class JwtProtectedHandler extends VertxJsonRoute<EmptyRequest, SubjectResponse, MyApplicationState> {

    private final JwtProvider jwtProvider;

    public JwtProtectedHandler(JwtProvider jwtProvider) {
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
