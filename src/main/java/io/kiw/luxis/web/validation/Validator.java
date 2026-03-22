package io.kiw.luxis.web.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Validator<T> {
    final T value;
    private final String prefix;
    final Map<String, List<String>> errors = new LinkedHashMap<>();

    public Validator(final T value, final String prefix) {
        this.value = value;
        this.prefix = prefix;
    }

    public FieldChain jsonField(final String field, final Function<T, ?> getter) {
        Object resolved;
        try {
            resolved = getter.apply(value);
        } catch (final NullPointerException e) {
            resolved = null;
        }
        return new FieldChain(prefix + field, resolved, this);
    }

    public <N> Validator<T> jsonField(final String name, final Function<T, N> getter, final Consumer<Validator<N>> block) {
        N nested;
        try {
            nested = getter.apply(value);
        } catch (final NullPointerException e) {
            nested = null;
        }
        if (nested != null) {
            final Validator<N> nestedValidator = new Validator<>(nested, prefix + name + ".");
            block.accept(nestedValidator);
            errors.putAll(nestedValidator.errors);
        }
        return this;
    }

    public <E> ListValidator<T, E> listField(final String name, final Function<T, List<E>> getter) {
        List<E> list;
        try {
            list = getter.apply(value);
        } catch (final NullPointerException e) {
            list = null;
        }
        return new ListValidator<>(prefix + name, list, this);
    }

    void addError(final String field, final String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }
}
