package io.kiw.luxis.web.test.handler;

import java.util.Map;

public class FileUploadResponse {
    public final Map<String, Integer> results;

    public FileUploadResponse(Map<String, Integer> results)
    {
        this.results = results;
    }
}
