package io.kiw.luxis.web.http;

public class HttpErrorResponseException extends RuntimeException {
    private final HttpErrorResponse errorResponse;

    public HttpErrorResponseException(final HttpErrorResponse errorResponse) {
        super(errorResponse.errorMessageValue().toString());
        this.errorResponse = errorResponse;
    }

    public HttpErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
