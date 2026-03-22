package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.handler.VertxFileDownloadRoute;
import io.kiw.luxis.web.http.DownloadFileResponse;
import io.kiw.luxis.web.http.HttpBuffer;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpMapStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class FileDownloaderHandler implements VertxFileDownloadRoute<String, MyApplicationState> {
    @Override
    public RequestPipeline<DownloadFileResponse> handle(final HttpMapStream<String, MyApplicationState> e) {
        return e.complete(ctx ->
            Result.success(new DownloadFileResponse(HttpBuffer.fromString("file contents"), "data.txt")));
    }
}
