package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.http.DownloadFileResponse;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpMapStream;

public interface FileDownloadRoute<IN, APP> {
    RequestPipeline<DownloadFileResponse> handle(HttpMapStream<IN, APP> e);
}
