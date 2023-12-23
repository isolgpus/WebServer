package io.kiw.result;

public class NoOpValidationResult<I> implements ValidationResult<I> {
    @Override
    public ValidationResult<I> validate(ValidationLogic<I> validationLogic, String errorMessage) {
        return this;
    }
}
