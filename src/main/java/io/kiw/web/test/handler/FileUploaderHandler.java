package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;

import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.http.HttpResult;
import io.kiw.web.handler.VertxFileUploadRoute;
import io.vertx.core.buffer.Buffer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FileUploaderHandler extends VertxFileUploadRoute<FileUploadResponse, MyApplicationState> {
    @Override
    public RequestPipeline<FileUploadResponse> handle(HttpStream<Map<String, Buffer>, MyApplicationState> e) {
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
