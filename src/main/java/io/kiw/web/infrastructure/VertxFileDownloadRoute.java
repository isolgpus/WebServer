package io.kiw.web.infrastructure;

public interface VertxFileDownloadRoute<IN, APP> {
    RequestPipeline<DownloadFileResponse> handle(HttpStream<IN, APP> e);
}
