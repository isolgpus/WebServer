package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

import static io.kiw.web.infrastructure.HttpResult.success;

public class ValidationTestHandler extends VertxJsonRoute<ValidationRequest, ValidationResponse, MyApplicationState> {

    @Override
    public RequestPipeline<ValidationResponse> handle(HttpResponseStream<ValidationRequest, MyApplicationState> httpResponseStream) {
        return httpResponseStream
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
