package io.kiw.luxis.web.validation;

import java.util.function.Predicate;

public class FieldChain {
    private final String fieldName;
    private final Object value;
    private final Validator<?> parent;

    FieldChain(String fieldName, Object value, Validator<?> parent) {
        this.fieldName = fieldName;
        this.value = value;
        this.parent = parent;
    }

    public FieldChain required() {
        if (value == null || (value instanceof String s && s.isBlank())) {
            parent.addError(fieldName, "must not be blank");
        }
        return this;
    }

    public FieldChain minLength(int min) {
        if (value instanceof String s && s.length() < min) {
            parent.addError(fieldName, "must be at least " + min + " characters");
        }
        return this;
    }

    public FieldChain maxLength(int max) {
        if (value instanceof String s && s.length() > max) {
            parent.addError(fieldName, "must be at most " + max + " characters");
        }
        return this;
    }

    public FieldChain email() {
        if (value instanceof String s && !s.isBlank() && !s.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            parent.addError(fieldName, "must be a valid email address");
        }
        return this;
    }

    public FieldChain matches(String regex) {
        if (value instanceof String s && !s.matches(regex)) {
            parent.addError(fieldName, "must match pattern: " + regex);
        }
        return this;
    }

    public FieldChain min(double min) {
        if (value instanceof Number n && n.doubleValue() < min) {
            parent.addError(fieldName, "must be at least " + min);
        }
        return this;
    }

    public FieldChain max(double max) {
        if (value instanceof Number n && n.doubleValue() > max) {
            parent.addError(fieldName, "must be at most " + max);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <V> FieldChain validate(Predicate<V> predicate, String message) {
        if (value != null && !predicate.test((V) value)) {
            parent.addError(fieldName, message);
        }
        return this;
    }
}
