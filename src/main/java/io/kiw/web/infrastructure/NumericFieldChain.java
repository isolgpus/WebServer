package io.kiw.web.infrastructure;

public class NumericFieldChain {
    private final String fieldName;
    private final Number value;
    private final Validator<?> parent;

    NumericFieldChain(String fieldName, Number value, Validator<?> parent) {
        this.fieldName = fieldName;
        this.value = value;
        this.parent = parent;
    }

    public NumericFieldChain required() {
        if (value == null) {
            parent.addError(fieldName, "must not be null");
        }
        return this;
    }

    public NumericFieldChain min(double min) {
        if (value != null && value.doubleValue() < min) {
            parent.addError(fieldName, "must be at least " + min);
        }
        return this;
    }

    public NumericFieldChain max(double max) {
        if (value != null && value.doubleValue() > max) {
            parent.addError(fieldName, "must be at most " + max);
        }
        return this;
    }
}
