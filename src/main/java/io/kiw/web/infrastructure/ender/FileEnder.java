package io.kiw.web.infrastructure.ender;

import io.kiw.web.infrastructure.DownloadFileResponse;
import io.kiw.web.infrastructure.VertxContext;

import java.nio.Buffer;

public final class FileEnder implements Ender {

    @Override
    public <T> void end(VertxContext vertxContext, T value) {
        DownloadFileResponse value1 = (DownloadFileResponse) value;
        vertxContext.addResponseHeader("Content-Disposition", value1.fileName);
        vertxContext.end(value1.fileContents);
    }
}
