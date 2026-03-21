package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class ValidationTestHandler extends VertxJsonRoute<ValidationRequest, ValidationResponse, MyApplicationState> {

    @Override
    public RequestPipeline<ValidationResponse> handle(HttpStream<ValidationRequest, MyApplicationState> httpStream) {
        return httpStream
            .validate(v -> {
                v.jsonField("name", r -> r.name).required().minLength(2);
                v.jsonField("email", r -> r.email).required().email();
                v.jsonField("age", r -> r.age).required().min(0).max(150);
                v.jsonField("address", r -> r.address, a -> {
                    a.jsonField("city", x -> x.city).required();
                    a.jsonField("zip", x -> x.zip).required().matches("[0-9]{5}");
                });
                v.queryParam("page").required().matches("[0-9]+");
                v.pathParam("userId").required().matches("[0-9]+");
            })
            .complete(ctx -> {
                ValidationRequest r = ctx.in();
                String page = ctx.http().getQueryParam("page");
                String userId = ctx.http().getPathParam("userId");
                return success(new ValidationResponse(r.name, r.email, r.age, r.address.city, page, userId));
            });
    }
}
