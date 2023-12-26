package io.kiw.web.test;

import io.kiw.result.Result;
import io.kiw.web.infrastructure.*;
import io.vertx.core.buffer.Buffer;

public class FileDownloaderHandler implements VertxFileDownloadRoute<String, MyApplicationState> {
    @Override
    public Flow<DownloadFileResponse> handle(HttpResponseStream<String, MyApplicationState> e) {
        return e.complete((request, context, myApplicationState) ->
            Result.success(new DownloadFileResponse(Buffer.buffer("file contents"), "data.txt")));
    }
}
