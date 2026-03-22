package io.kiw.luxis.web.validation;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;

public class WebSocketValidator<T> extends Validator<T> {

    public WebSocketValidator(final T value, final String prefix) {
        super(value, prefix);
    }

    public Result<ErrorMessageResponse, T> toResult() {
        if (errors.isEmpty()) {
            return Result.success(value);
        }
        return Result.error(new ErrorMessageResponse("Validation failed", errors));
    }
}
