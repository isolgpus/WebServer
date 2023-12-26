package io.kiw.web.test;

import io.kiw.web.infrastructure.DownloadFileResponse;
import io.kiw.web.infrastructure.Flow;
import io.kiw.web.infrastructure.HttpResponseStream;

public interface VertxFileDownloadRoute<IN, APP> {
    Flow<DownloadFileResponse> handle(HttpResponseStream<IN, APP> e);
}
