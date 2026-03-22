package io.kiw.luxis.web.validation;

import java.util.List;
import java.util.function.Consumer;

public class ListValidator<P, E> {
    private final String fieldName;
    private final List<E> list;
    private final Validator<P> parent;

    ListValidator(final String fieldName, final List<E> list, final Validator<P> parent) {
        this.fieldName = fieldName;
        this.list = list;
        this.parent = parent;
    }

    public ListValidator<P, E> required() {
        if (list == null) parent.addError(fieldName, "must not be null");
        return this;
    }

    public ListValidator<P, E> minSize(final int min) {
        if (list != null && list.size() < min) {
            parent.addError(fieldName, "must have at least " + min + " items");
        }
        return this;
    }

    public ListValidator<P, E> maxSize(final int max) {
        if (list != null && list.size() > max) {
            parent.addError(fieldName, "must have at most " + max + " items");
        }
        return this;
    }

    public Validator<P> each(final Consumer<Validator<E>> block) {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                final String elementPrefix = fieldName + "[" + i + "].";
                final Validator<E> ev = new Validator<>(list.get(i), elementPrefix);
                block.accept(ev);
                parent.errors.putAll(ev.errors);
            }
        }
        return parent;
    }
}
