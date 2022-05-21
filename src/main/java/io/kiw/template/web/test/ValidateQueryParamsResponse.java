package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.JsonResponse;

public class ValidateQueryParamsResponse implements JsonResponse {
    public final String required;
    public final Integer rangedInt;

    public ValidateQueryParamsResponse(String required, Integer rangedInt) {

        this.required = required;
        this.rangedInt = rangedInt;
    }
}
