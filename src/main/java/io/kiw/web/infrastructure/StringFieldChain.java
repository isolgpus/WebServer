package io.kiw.web.infrastructure;

public class StringFieldChain {
    private final String fieldName;
    private final String value;
    private final Validator<?> parent;

    StringFieldChain(String fieldName, String value, Validator<?> parent) {
        this.fieldName = fieldName;
        this.value = value;
        this.parent = parent;
    }

    public StringFieldChain required() {
        if (value == null || value.isBlank()) {
            parent.addError(fieldName, "must not be blank");
        }
        return this;
    }

    public StringFieldChain minLength(int min) {
        if (value != null && value.length() < min) {
            parent.addError(fieldName, "must be at least " + min + " characters");
        }
        return this;
    }

    public StringFieldChain maxLength(int max) {
        if (value != null && value.length() > max) {
            parent.addError(fieldName, "must be at most " + max + " characters");
        }
        return this;
    }

    public StringFieldChain email() {
        if (value != null && !value.isBlank() && !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            parent.addError(fieldName, "must be a valid email address");
        }
        return this;
    }

    public StringFieldChain matches(String regex) {
        if (value != null && !value.matches(regex)) {
            parent.addError(fieldName, "must match pattern: " + regex);
        }
        return this;
    }
}
