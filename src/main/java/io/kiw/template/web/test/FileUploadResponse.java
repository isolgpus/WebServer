package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.JsonResponse;

import java.util.Map;

public class FileUploadResponse implements JsonResponse {
    public final Map<String, Integer> results;

    public FileUploadResponse(Map<String, Integer> results)
    {
        this.results = results;
    }
}
