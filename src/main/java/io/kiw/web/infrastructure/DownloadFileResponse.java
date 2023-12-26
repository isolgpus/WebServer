package io.kiw.web.infrastructure;

import io.vertx.core.buffer.Buffer;

public class DownloadFileResponse {
    public final Buffer fileContents;
    public final String fileName;

    public DownloadFileResponse(Buffer fileContents, String fileName) {
        this.fileContents = fileContents;
        this.fileName = fileName;
    }
}
