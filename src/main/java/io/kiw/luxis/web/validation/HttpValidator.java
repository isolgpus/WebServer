package io.kiw.luxis.web.validation;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpContext;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;

public class HttpValidator<T> extends Validator<T> {
    private final HttpContext http;

    public HttpValidator(final T value, final HttpContext http, final String prefix) {
        super(value, prefix);
        this.http = http;
    }

    public FieldChain queryParam(final String name) {
        return new FieldChain(name, http.getQueryParam(name), this);
    }

    public FieldChain pathParam(final String name) {
        return new FieldChain(name, http.getPathParam(name), this);
    }

    public Result<HttpErrorResponse, T> toResult() {
        if (errors.isEmpty()) {
            return Result.success(value);
        }
        return HttpResult.error(ErrorStatusCode.UNPROCESSABLE_ENTITY, new ErrorMessageResponse("Validation failed", errors));
    }
}
