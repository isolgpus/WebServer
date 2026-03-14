package io.kiw.web.test;

import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.VertxFileUploadRoute;
import io.vertx.core.buffer.Buffer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FileUploaderHandler extends VertxFileUploadRoute<FileUploadResponse, MyApplicationState> {
    @Override
    public RequestPipeline<FileUploadResponse> handle(HttpResponseStream<Map<String, Buffer>, MyApplicationState> e) {
        return e.complete(ctx -> {
            Map<String, Integer> results = ctx.in().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                me -> me.getValue().getBytes().length,
                (integer, integer2) -> integer,
                LinkedHashMap::new));
            return HttpResult.success(new FileUploadResponse(results));
        });
    }
}
