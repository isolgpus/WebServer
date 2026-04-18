package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class JwtProtectedHandler extends JsonHandler<Void, SubjectResponse, MyApplicationState> {

    private final JwtProvider jwtProvider;

    public JwtProtectedHandler(final JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public RequestPipeline<SubjectResponse> handle(final HttpStream<Void, MyApplicationState> stream) {
        return stream
                .requireJwt(jwtProvider)
                .complete(ctx ->
                        HttpResult.success(new SubjectResponse(ctx.session().getJwtClaims().getSubject())));
    }
}
