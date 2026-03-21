package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;

import io.vertx.core.buffer.Buffer;

public class FileUploadRequest {
    private final String name;
    private final Buffer buffer;

    public FileUploadRequest(String name, Buffer buffer) {
        this.name = name;
        this.buffer = buffer;
    }
}
