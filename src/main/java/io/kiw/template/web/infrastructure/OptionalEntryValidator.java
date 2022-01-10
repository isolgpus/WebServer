package io.kiw.template.web.infrastructure;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class OptionalEntryValidator<IN, T> {
    private final MapValidator<T> mapValidator;
    private final Optional<IN> value;
    private final Optional<String> validationError;
    private final boolean isHealthy;
    private final String key;

    OptionalEntryValidator(MapValidator<T> mapValidator, final String key, Optional<IN> value, Optional<String> validationError, boolean isHealthy) {
        this.mapValidator = mapValidator;
        this.value = value;
        this.key = key;
        this.validationError = validationError;
        this.isHealthy = isHealthy;
    }


    public OptionalEntryValidator<IN, T> validate(final Predicate<IN> check, final String errorMessage)
    {
        if(isHealthy && this.value.isPresent())
        {
            boolean result = check.test(this.value.get());
            if(!result)
            {
                return new OptionalEntryValidator<>(mapValidator, this.key, this.value, Optional.of(errorMessage), false);
            }
        }

        return this;
    }

    public <OUT> OptionalEntryValidator<OUT, T> attemptMap(final Function<IN, OUT> mapper, final String errorMessage)
    {
        if(isHealthy && this.value.isPresent())
        {
            try
            {
                final Optional<OUT> apply = this.value.map(mapper);
                return new OptionalEntryValidator<>(mapValidator, this.key, apply, this.validationError, true);
            }
            catch (Exception e)
            {
                return new OptionalEntryValidator<>(mapValidator, this.key, Optional.empty(), Optional.of(errorMessage), false);
            }
        }
        else
        {
            return new OptionalEntryValidator<>(mapValidator, this.key, Optional.empty(), this.validationError, false);
        }
    }

    public <OUT> OptionalEntryValidator<OUT, T> attemptFlatMap(final Function<IN, Optional<OUT>> mapper, final String errorMessage)
    {
        if(isHealthy && this.value.isPresent())
        {
            try
            {
                final Optional<OUT> apply = this.value.flatMap(mapper);
                return new OptionalEntryValidator<>(mapValidator, this.key, apply, this.validationError, true);
            }
            catch (Exception e)
            {
                return new OptionalEntryValidator<>(mapValidator, this.key, Optional.empty(), Optional.of(errorMessage), false);
            }
        }
        else
        {
            return new OptionalEntryValidator<>(mapValidator, this.key, Optional.empty(), this.validationError, false);
        }
    }

    public MapValidator<T> next() {
        validationError.ifPresent(e -> this.mapValidator.addValidationError(key, e));
        this.mapValidator.addValidatedValue(key, value);
        return this.mapValidator;
    }
}
