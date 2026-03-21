package io.kiw.luxis.web.http;

public class DownloadFileResponse {
    public final HttpBuffer fileContents;
    public final String fileName;

    public DownloadFileResponse(final HttpBuffer fileContents, final String fileName) {
        this.fileContents = fileContents;
        this.fileName = fileName;
    }
}
