package io.kiw.web.http;

import java.nio.charset.StandardCharsets;

public record HttpBuffer(byte[] bytes) {
    public static HttpBuffer fromString(String s) {
        return new HttpBuffer(s.getBytes(StandardCharsets.UTF_8));
    }

    public String toString(java.nio.charset.Charset charset) {
        return new String(bytes, charset);
    }
}
