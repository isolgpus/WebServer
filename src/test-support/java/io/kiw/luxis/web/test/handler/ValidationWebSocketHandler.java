package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class ValidationWebSocketHandler extends WebSocketRoute<ValidationRequest, WebSocketValidationResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketValidationResponse> onMessage(final WebSocketStream<ValidationRequest, MyApplicationState> stream) {
        return stream
            .validate(v -> {
                v.jsonField("name", r -> r.name).required().minLength(2);
                v.jsonField("email", r -> r.email).required().email();
                v.jsonField("age", r -> r.age).required().min(0).max(150);
                v.jsonField("address", r -> r.address, a -> {
                    a.jsonField("city", x -> x.city).required();
                    a.jsonField("zip", x -> x.zip).required().matches("[0-9]{5}");
                });
            })
            .map(ctx -> {
                final ValidationRequest r = ctx.in();
                return new WebSocketValidationResponse(r.name, r.email, r.age, r.address.city);
            })
            .complete();
    }
}
