package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxFileUploadRoute;
import io.vertx.core.buffer.Buffer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FileUploaderHandler extends VertxFileUploadRoute<FileUploadResponse, MyApplicationState> {
    @Override
    public Flow<FileUploadResponse> handle(HttpControlStream<Map<String, Buffer>, MyApplicationState> e) {
        return e.complete((request, httpContext, applicationState) -> {
            Map<String, Integer> results = request.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                me -> me.getValue().getBytes().length,
                (integer, integer2) -> integer,
                LinkedHashMap::new));
            return HttpResult.success(new FileUploadResponse(results));
        });
    }
}
