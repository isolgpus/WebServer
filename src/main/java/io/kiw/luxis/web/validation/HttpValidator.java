package io.kiw.luxis.web.validation;

import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpSession;

public class HttpValidator<T> extends Validator<T, HttpErrorResponse> {
    private final HttpSession http;

    public HttpValidator(final T value, final HttpSession http, final String prefix) {
        super(value, prefix, (e, cause) -> new HttpErrorResponse(e, ErrorStatusCode.UNPROCESSABLE_ENTITY));
        this.http = http;
    }

    public FieldChain queryParam(final String name) {
        return new FieldChain(name, http.getQueryParam(name), this);
    }

    public FieldChain pathParam(final String name) {
        return new FieldChain(name, http.getPathParam(name), this);
    }

}
