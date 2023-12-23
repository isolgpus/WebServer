package io.kiw.result;

public interface ValidationResult<I> {
    ValidationResult<I> validate(ValidationLogic<I> validationLogic, String errorMessage);
}
