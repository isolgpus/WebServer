package io.kiw.web.infrastructure;

public interface VertxFileDownloadRoute<IN, APP> {
    RequestPipeline<DownloadFileResponse> handle(HttpResponseStream<IN, APP> e);
}
