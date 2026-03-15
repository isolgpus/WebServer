package io.kiw.web.infrastructure;

public class HttpErrorResponse {
    final ErrorMessageResponse errorMessageValue;
    final int statusCode;

    public HttpErrorResponse(ErrorMessageResponse errorMessageValue, ErrorStatusCode statusCode) {
        this.errorMessageValue = errorMessageValue;
        this.statusCode = statusCode.code();
    }
}
