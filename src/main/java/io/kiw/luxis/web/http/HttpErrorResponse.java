package io.kiw.luxis.web.http;

import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.validation.*;

public class HttpErrorResponse {
    public final ErrorMessageResponse errorMessageValue;
    public final int statusCode;

    public HttpErrorResponse(ErrorMessageResponse errorMessageValue, ErrorStatusCode statusCode) {
        this.errorMessageValue = errorMessageValue;
        this.statusCode = statusCode.code();
    }
}
