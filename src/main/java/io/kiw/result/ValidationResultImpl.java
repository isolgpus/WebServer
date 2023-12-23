package io.kiw.result;

public class ValidationResultImpl<I> implements ValidationResult<I> {
    private final String fieldDescription;
    private final I value;
    private final CollectiveValidationErrors collectiveValidationErrors;

    public ValidationResultImpl(String fieldDescription, I value, CollectiveValidationErrors collectiveValidationErrors) {
        this.fieldDescription = fieldDescription;
        this.value = value;
        this.collectiveValidationErrors = collectiveValidationErrors;
    }

    @Override
    public ValidationResult<I> validate(ValidationLogic<I> validationLogic, String errorMessage) {
        boolean valid = validationLogic.validate(this.value);

        if (!valid) {
            collectiveValidationErrors.addError(fieldDescription, errorMessage);
        }
        return this;
    }
}
