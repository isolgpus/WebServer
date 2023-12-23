package io.kiw.result;

public abstract class Validation<I> {


    public static <I extends Validatable> Result<CollectiveValidationErrors, I> validate(String description, I value) {
        final CollectiveValidationErrors collectiveValidationErrors = new CollectiveValidationErrors();
        if (value == null) {
            collectiveValidationErrors.addError(description, "must be provided");
            return new Result.Error<>(collectiveValidationErrors);
        }

        final Validator validator = new Validator(collectiveValidationErrors);
        value.validate(validator);

        if (!validator.hasErrors()) {
            return new Result.Success<>(value);
        } else {
            return new Result.Error<>(collectiveValidationErrors);
        }
    }


}