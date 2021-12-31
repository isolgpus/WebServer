package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class VertxJsonRoute <REQ extends JsonRequest, RES extends JsonResponse> extends TypeReference<REQ> {


    public abstract Flow<RES> handle(HttpControlStream<REQ> e);

}
