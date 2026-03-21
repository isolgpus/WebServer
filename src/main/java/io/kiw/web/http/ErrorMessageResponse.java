package io.kiw.web.http;

import io.kiw.web.jwt.*;
import io.kiw.web.validation.*;

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
