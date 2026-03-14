package io.kiw.web.infrastructure;

import io.kiw.result.Result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Validator<T> {
    private final T value;
    private final HttpContext http;
    private final String prefix;
    final Map<String, List<String>> errors = new LinkedHashMap<>();

    Validator(T value, HttpContext http, String prefix) {
        this.value = value;
        this.http = http;
        this.prefix = prefix;
    }

    public StringFieldChain jsonField(String field, Function<T, String> getter) {
        String resolved;
        try {
            resolved = getter.apply(value);
        } catch (NullPointerException e) {
            resolved = null;
        }
        return new StringFieldChain(prefix + field, resolved, this);
    }

    public NumericFieldChain numericJsonField(String field, Function<T, ? extends Number> getter) {
        Number resolved;
        try {
            resolved = getter.apply(value);
        } catch (NullPointerException e) {
            resolved = null;
        }
        return new NumericFieldChain(prefix + field, resolved, this);
    }

    public <N> Validator<T> jsonField(String name, Function<T, N> getter, Consumer<Validator<N>> block) {
        N nested;
        try {
            nested = getter.apply(value);
        } catch (NullPointerException e) {
            nested = null;
        }
        if (nested != null) {
            Validator<N> nestedValidator = new Validator<>(nested, http, prefix + name + ".");
            block.accept(nestedValidator);
            errors.putAll(nestedValidator.errors);
        }
        return this;
    }

    public StringFieldChain queryParam(String name) {
        return new StringFieldChain(prefix + name, http.getQueryParam(name), this);
    }

    public StringFieldChain pathParam(String name) {
        return new StringFieldChain(prefix + name, http.getPathParam(name), this);
    }

    void addError(String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }

    Result<HttpErrorResponse, T> toResult() {
        if (errors.isEmpty()) {
            return Result.success(value);
        }
        return HttpResult.error(422, new ErrorMessageResponse("Validation failed", errors));
    }
}
