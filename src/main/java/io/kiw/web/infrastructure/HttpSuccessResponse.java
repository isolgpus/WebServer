package io.kiw.web.infrastructure;

public class HttpSuccessResponse<S> {
    public final S value;
    public final int statusCode;

    public HttpSuccessResponse(S value, int statusCode) {
        this.value = value;
        this.statusCode = statusCode;
    }
}
