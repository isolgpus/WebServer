package io.kiw.web.internal.ender;

import io.kiw.web.http.DownloadFileResponse;
import io.kiw.web.http.RequestContext;

public final class FileEnder implements Ender {

    @Override
    public <T> void end(RequestContext requestContext, T value) {
        DownloadFileResponse value1 = (DownloadFileResponse) value;
        requestContext.addResponseHeader("Content-Disposition", value1.fileName);
        requestContext.end(value1.fileContents);
    }
}
