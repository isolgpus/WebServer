package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class JwtFilterProtectedHandler extends JsonHandler<Void, SubjectResponse, MyApplicationState> {


    @Override
    public RequestPipeline<SubjectResponse> handle(final HttpStream<Void, MyApplicationState> stream) {
        return stream
                .complete(ctx ->
                        HttpResult.success(new SubjectResponse(ctx.http().getJwtClaims().getSubject())));
    }
}
