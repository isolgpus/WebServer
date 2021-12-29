package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class VertxJsonRoute <T extends JsonRequest> extends TypeReference<T> {


    public abstract Object handle(T jsonRequest, HttpContext httpContext);

}
