package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;

import io.kiw.result.Result;
import io.kiw.web.infrastructure.*;
import io.vertx.core.buffer.Buffer;

public class FileDownloaderHandler implements VertxFileDownloadRoute<String, MyApplicationState> {
    @Override
    public RequestPipeline<DownloadFileResponse> handle(HttpResponseStream<String, MyApplicationState> e) {
        return e.complete(ctx ->
            Result.success(new DownloadFileResponse(Buffer.buffer("file contents"), "data.txt")));
    }
}
