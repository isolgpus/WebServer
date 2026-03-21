package io.kiw.web.http;

import io.kiw.web.jwt.*;
import io.kiw.web.validation.*;

import io.vertx.core.buffer.Buffer;

public class DownloadFileResponse {
    public final Buffer fileContents;
    public final String fileName;

    public DownloadFileResponse(Buffer fileContents, String fileName) {
        this.fileContents = fileContents;
        this.fileName = fileName;
    }
}
