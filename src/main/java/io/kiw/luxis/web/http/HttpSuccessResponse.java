package io.kiw.luxis.web.http;

import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.validation.*;

public class HttpSuccessResponse<S> {
    public final S value;
    public final int statusCode;

    public HttpSuccessResponse(S value, SuccessStatusCode statusCode) {
        this.value = value;
        this.statusCode = statusCode.code();
    }
}
