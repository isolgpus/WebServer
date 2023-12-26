package io.kiw.web.test;

import io.kiw.web.infrastructure.JsonResponse;

import java.util.Map;

public class FileUploadResponse {
    public final Map<String, Integer> results;

    public FileUploadResponse(Map<String, Integer> results)
    {
        this.results = results;
    }
}
