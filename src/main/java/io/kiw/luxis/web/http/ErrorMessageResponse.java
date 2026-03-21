package io.kiw.luxis.web.http;

import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.validation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ErrorMessageResponse {
    public final String message;
    public final Map<String, List<String>> errors;

    public ErrorMessageResponse(String message) {
        this.message = message;
        this.errors = Collections.emptyMap();
    }

    public ErrorMessageResponse(String message, Map<String, List<String>> errors) {
        this.message = message;
        this.errors = errors;
    }
}
