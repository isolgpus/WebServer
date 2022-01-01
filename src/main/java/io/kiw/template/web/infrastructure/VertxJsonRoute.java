package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class VertxJsonRoute <IN extends JsonRequest, OUT extends JsonResponse, APP> extends TypeReference<IN> {


    public abstract Flow<OUT> handle(HttpControlStream<IN, APP> e);

}
