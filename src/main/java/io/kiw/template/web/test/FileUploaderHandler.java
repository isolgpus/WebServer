package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxFileUploadRoute;
import io.vertx.core.buffer.Buffer;

import java.util.Map;

public class FileUploaderHandler extends VertxFileUploadRoute<FileUploadResponse, MyApplicationState> {
    @Override
    public Flow<FileUploadResponse> handle(HttpControlStream<Map<String, Buffer>, MyApplicationState> e) {
        return e.complete((request, httpContext, applicationState) -> HttpResult.success(new FileUploadResponse()));
    }
}
