package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;

public class JwtFilterProtectedHandler extends VertxJsonRoute<EmptyRequest, SubjectResponse, MyApplicationState> {


    @Override
    public RequestPipeline<SubjectResponse> handle(HttpStream<EmptyRequest, MyApplicationState> stream) {
        return stream
            .complete(ctx ->
                HttpResult.success(new SubjectResponse(ctx.http().getJwtClaims().getSubject())));
    }
}
