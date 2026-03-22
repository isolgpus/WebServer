package io.kiw.luxis.web.http;

public record BlockingContext<IN>(IN in, HttpContext http) {
}
