package io.kiw.web.http;

import io.kiw.web.jwt.*;
import io.kiw.web.validation.*;

public class HttpErrorResponse {
    public final ErrorMessageResponse errorMessageValue;
    public final int statusCode;

    public HttpErrorResponse(ErrorMessageResponse errorMessageValue, ErrorStatusCode statusCode) {
        this.errorMessageValue = errorMessageValue;
        this.statusCode = statusCode.code();
    }
}
