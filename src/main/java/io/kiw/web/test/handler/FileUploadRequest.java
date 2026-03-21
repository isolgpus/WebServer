package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;

public class FileUploadRequest {
    private final String name;
    private final HttpBuffer buffer;

    public FileUploadRequest(String name, HttpBuffer buffer) {
        this.name = name;
        this.buffer = buffer;
    }
}
