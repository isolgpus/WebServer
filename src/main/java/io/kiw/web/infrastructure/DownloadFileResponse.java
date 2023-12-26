package io.kiw.web.infrastructure;

public class DownloadFileResponse {
    public final String fileContents;
    public final String fileName;

    public DownloadFileResponse(String fileContents, String fileName) {
        this.fileContents = fileContents;
        this.fileName = fileName;
    }
}
