package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;

import io.kiw.result.Result;
import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.vertx.core.buffer.Buffer;

public class FileDownloaderHandler implements VertxFileDownloadRoute<String, MyApplicationState> {
    @Override
    public RequestPipeline<DownloadFileResponse> handle(HttpStream<String, MyApplicationState> e) {
        return e.complete(ctx ->
            Result.success(new DownloadFileResponse(Buffer.buffer("file contents"), "data.txt")));
    }
}
