package io.kiw.web.infrastructure;

public class HttpSuccessResponse<S> {
    public final S value;
    public final int statusCode;

    public HttpSuccessResponse(S value, SuccessStatusCode statusCode) {
        this.value = value;
        this.statusCode = statusCode.code();
    }
}
