package io.kiw.web.test;

import io.vertx.core.buffer.Buffer;

public class FileUploadRequest {
    private final String name;
    private final Buffer buffer;

    public FileUploadRequest(String name, Buffer buffer) {
        this.name = name;
        this.buffer = buffer;
    }
}
