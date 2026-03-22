package io.kiw.luxis.web.validation;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class WebSocketValidator<T> {
    private final T value;
    private final Validator<T> delegate;

    WebSocketValidator(final T value, final String prefix) {
        this.value = value;
        this.delegate = new Validator<>(value, null, prefix);
    }

    public FieldChain jsonField(final String field, final Function<T, ?> getter) {
        return delegate.jsonField(field, getter);
    }

    public <N> WebSocketValidator<T> jsonField(final String name, final Function<T, N> getter, final Consumer<Validator<N>> block) {
        delegate.jsonField(name, getter, block);
        return this;
    }

    public <E> ListValidator<T, E> listField(final String name, final Function<T, List<E>> getter) {
        return delegate.listField(name, getter);
    }

    public Result<ErrorMessageResponse, T> toResult() {
        if (delegate.errors.isEmpty()) {
            return Result.success(value);
        }
        return Result.error(new ErrorMessageResponse("Validation failed", delegate.errors));
    }
}
