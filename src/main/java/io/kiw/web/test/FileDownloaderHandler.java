package io.kiw.web.test;

import io.kiw.result.Result;
import io.kiw.web.infrastructure.*;

public class FileDownloaderHandler implements VertxFileDownloadRoute<String, MyApplicationState> {
    @Override
    public Flow<DownloadFileResponse> handle(HttpResponseStream<String, MyApplicationState> e) {
        return e.complete((request, context, myApplicationState) -> Result.success(new DownloadFileResponse("file contents", "data.txt")));
    }
}
