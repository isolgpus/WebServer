package io.kiw.luxis.web.validation;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpContext;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Validator<T> {
    private final T value;
    final HttpContext http;
    private final String prefix;
    final Map<String, List<String>> errors = new LinkedHashMap<>();

    public Validator(T value, HttpContext http, String prefix) {
        this.value = value;
        this.http = http;
        this.prefix = prefix;
    }

    public FieldChain jsonField(String field, Function<T, ?> getter) {
        Object resolved;
        try {
            resolved = getter.apply(value);
        } catch (NullPointerException e) {
            resolved = null;
        }
        return new FieldChain(prefix + field, resolved, this);
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

    public <E> ListValidator<T, E> listField(String name, Function<T, List<E>> getter) {
        List<E> list;
        try {
            list = getter.apply(value);
        } catch (NullPointerException e) {
            list = null;
        }
        return new ListValidator<>(prefix + name, list, this);
    }

    public FieldChain queryParam(String name) {
        return new FieldChain(prefix + name, http.getQueryParam(name), this);
    }

    public FieldChain pathParam(String name) {
        return new FieldChain(prefix + name, http.getPathParam(name), this);
    }

    void addError(String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }

    public Result<HttpErrorResponse, T> toResult() {
        if (errors.isEmpty()) {
            return Result.success(value);
        }
        return HttpResult.error(ErrorStatusCode.UNPROCESSABLE_ENTITY, new ErrorMessageResponse("Validation failed", errors));
    }
}
