package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.test.MyApplicationState;

public class JwtFilterProtectedHandler extends VertxJsonRoute<EmptyRequest, SubjectResponse, MyApplicationState> {


    @Override
    public RequestPipeline<SubjectResponse> handle(HttpStream<EmptyRequest, MyApplicationState> stream) {
        return stream
            .complete(ctx ->
                HttpResult.success(new SubjectResponse(ctx.http().getJwtClaims().getSubject())));
    }
}
