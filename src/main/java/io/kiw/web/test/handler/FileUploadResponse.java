package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;

import io.kiw.web.http.JsonResponse;

import java.util.Map;

public class FileUploadResponse {
    public final Map<String, Integer> results;

    public FileUploadResponse(Map<String, Integer> results)
    {
        this.results = results;
    }
}
