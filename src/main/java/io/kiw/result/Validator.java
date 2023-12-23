package io.kiw.result;

import java.util.List;

public class Validator {


    private final CollectiveValidationErrors collectiveValidationErrors;

    Validator(CollectiveValidationErrors collectiveValidationErrors) {

        this.collectiveValidationErrors = collectiveValidationErrors;
    }

    public <I> ValidationResult<I> required(String name, I value) {
        if (value == null) {
            collectiveValidationErrors.addError(name, "must be provided");
            return new NoOpValidationResult<>();
        }
        return new ValidationResultImpl<>(name, value, collectiveValidationErrors);
    }

    public <I extends Validatable> void required(String name, List<I> value) {
        if (value == null) {
            collectiveValidationErrors.addError(name, "must be provided");
            return;
        }
        for (I i : value) {
            collectiveValidationErrors.withinScope(name,
                () -> collectiveValidationErrors.withinScope(i + "", () -> i.validate(this)));
        }
    }

    public <I extends Validatable> void optional(String name, List<I> value) {
        if (value == null) {
            return;
        }
        for (I i : value) {
            collectiveValidationErrors.withinScope(name,
                () -> collectiveValidationErrors.withinScope(i + "", () -> i.validate(this)));
        }
    }

    public <I extends Validatable> void required(String name, I value) {
        if (value == null) {
            collectiveValidationErrors.addError(name, "must be provided");
        } else {
            collectiveValidationErrors.withinScope(name, () -> {
                value.validate(this);
            });
        }
    }

    public <I extends Validatable> void optional(String name, I value) {
        if (value != null) {
            collectiveValidationErrors.withinScope(name, () -> {
                value.validate(this);
            });
        }
    }

    public <I> ValidationResult<I> optional(String name, I value) {
        if (value == null) {
            return new NoOpValidationResult<>();
        }
        return new ValidationResultImpl<>(name, value, collectiveValidationErrors);
    }

    public boolean hasErrors() {
        return collectiveValidationErrors.hasErrors();
    }
}
