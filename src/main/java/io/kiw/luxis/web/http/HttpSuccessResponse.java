package io.kiw.luxis.web.http;

public class HttpSuccessResponse<S> {
    public final S value;
    public final int statusCode;

    public HttpSuccessResponse(final S value, final SuccessStatusCode statusCode) {
        this.value = value;
        this.statusCode = statusCode.code();
    }
}
