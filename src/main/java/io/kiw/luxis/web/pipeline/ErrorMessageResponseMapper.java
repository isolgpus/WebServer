package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.HttpErrorResponse;

public interface ErrorMessageResponseMapper<ERR> {
    ERR map(HttpErrorResponse httpErrorResponse);
}
