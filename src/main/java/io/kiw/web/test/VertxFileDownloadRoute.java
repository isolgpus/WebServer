package io.kiw.web.test;

import io.kiw.web.infrastructure.DownloadFileResponse;
import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.HttpResponseStream;

public interface VertxFileDownloadRoute<IN, APP> {
    RequestPipeline<DownloadFileResponse> handle(HttpResponseStream<IN, APP> e);
}
