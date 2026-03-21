package io.kiw.luxis.web.http;

public class HttpErrorResponse {
    public final ErrorMessageResponse errorMessageValue;
    public final int statusCode;

    public HttpErrorResponse(final ErrorMessageResponse errorMessageValue, final ErrorStatusCode statusCode) {
        this.errorMessageValue = errorMessageValue;
        this.statusCode = statusCode.code();
    }
}
