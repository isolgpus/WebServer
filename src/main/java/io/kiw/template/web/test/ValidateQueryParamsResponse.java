package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.JsonResponse;

import java.util.Optional;

public class ValidateQueryParamsResponse implements JsonResponse {
    public final String required;
    public final Integer rangedInt;
    public final Integer defaultedInt;
    public final Integer optionalInt;

    public ValidateQueryParamsResponse(String required, Integer rangedInt, Integer defaultedInt, Optional<Integer> optionalInt) {

        this.required = required;
        this.rangedInt = rangedInt;
        this.defaultedInt = defaultedInt;
        this.optionalInt = optionalInt.orElse(null);
    }
}
