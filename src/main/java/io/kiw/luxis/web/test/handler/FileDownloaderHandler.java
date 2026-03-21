package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.test.MyApplicationState;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;

public class FileDownloaderHandler implements VertxFileDownloadRoute<String, MyApplicationState> {
    @Override
    public RequestPipeline<DownloadFileResponse> handle(HttpStream<String, MyApplicationState> e) {
        return e.complete(ctx ->
            Result.success(new DownloadFileResponse(HttpBuffer.fromString("file contents"), "data.txt")));
    }
}
