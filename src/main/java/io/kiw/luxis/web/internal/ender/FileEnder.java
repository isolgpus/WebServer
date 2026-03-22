package io.kiw.luxis.web.internal.ender;

import io.kiw.luxis.web.http.DownloadFileResponse;
import io.kiw.luxis.web.http.RequestContext;

public final class FileEnder implements Ender {

    @Override
    public <T> void end(final RequestContext requestContext, final T value) {
        final DownloadFileResponse value1 = (DownloadFileResponse) value;
        requestContext.addResponseHeader("Content-Disposition", value1.fileName());
        requestContext.end(value1.fileContents());
    }
}
