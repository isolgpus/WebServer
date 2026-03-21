package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;

public class FileUploadRequest {
    private final String name;
    private final HttpBuffer buffer;

    public FileUploadRequest(String name, HttpBuffer buffer) {
        this.name = name;
        this.buffer = buffer;
    }
}
