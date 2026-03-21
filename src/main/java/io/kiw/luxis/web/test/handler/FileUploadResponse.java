package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;

import io.kiw.luxis.web.http.JsonResponse;

import java.util.Map;

public class FileUploadResponse {
    public final Map<String, Integer> results;

    public FileUploadResponse(Map<String, Integer> results)
    {
        this.results = results;
    }
}
