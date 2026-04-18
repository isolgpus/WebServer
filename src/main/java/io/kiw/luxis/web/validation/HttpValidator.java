package io.kiw.luxis.web.validation;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpSession;
import io.kiw.luxis.web.http.HttpErrorResponse;

public class HttpValidator<T> extends Validator<T> {
    private final HttpSession http;

    public HttpValidator(final T value, final HttpSession http, final String prefix) {
        super(value, prefix);
        this.http = http;
    }

    public FieldChain queryParam(final String name) {
        return new FieldChain(name, http.getQueryParam(name), this);
    }

    public FieldChain pathParam(final String name) {
        return new FieldChain(name, http.getPathParam(name), this);
    }

    public Result<HttpErrorResponse, T> toHttpResult() {
        return toResult().mapError(e -> new HttpErrorResponse(e, ErrorStatusCode.UNPROCESSABLE_ENTITY));
    }
}
