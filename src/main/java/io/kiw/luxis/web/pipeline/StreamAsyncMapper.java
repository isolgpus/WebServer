package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.client.LuxisAsync;

public interface StreamAsyncMapper<CTX, RES> {
    LuxisAsync<RES> handle(CTX ctx);
}
