package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class VertxJsonRoute<IN, OUT extends JsonResponse, APP> extends TypeReference<IN> {


    public abstract Flow<OUT> handle(HttpResponseStream<IN, APP> e);

}
