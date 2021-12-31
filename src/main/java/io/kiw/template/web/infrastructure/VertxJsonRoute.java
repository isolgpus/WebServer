package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class VertxJsonRoute <T extends JsonRequest, RES extends JsonResponse> extends TypeReference<T> {


    public abstract Flow<RES> handle(HttpControlStream<T> e);

}
