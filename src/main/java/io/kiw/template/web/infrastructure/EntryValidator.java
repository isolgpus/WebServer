package io.kiw.template.web.infrastructure;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntryValidator<IN> {
    private final MapValidator mapValidator;
    private final IN value;
    private final Optional<String> validationError;
    private final boolean isHealthy;
    private final String key;

    EntryValidator(MapValidator mapValidator, final String key, IN value, Optional<String> validationError, boolean isHealthy) {
        this.mapValidator = mapValidator;
        this.value = value;
        this.key = key;
        this.validationError = validationError;
        this.isHealthy = isHealthy;
    }


    public EntryValidator<IN> validate(final Predicate<IN> check, final String errorMessage)
    {
        if(isHealthy)
        {
            boolean result = check.test(this.value);
            if(!result)
            {
                return new EntryValidator<>(mapValidator, this.key, this.value, Optional.of(errorMessage), false);
            }
        }

        return this;
    }

    public <OUT> EntryValidator<OUT> attemptMap(final Function<IN, OUT> mapper, final String errorMessage)
    {
        if(isHealthy)
        {
            try
            {
                final OUT apply = mapper.apply(this.value);
                return new EntryValidator<>(mapValidator, this.key, apply, this.validationError, true);
            }
            catch (Exception e)
            {
                return new EntryValidator<>(mapValidator, this.key, null, Optional.of(errorMessage), false);
            }
        }
        else
        {
            return new EntryValidator<>(mapValidator, this.key, null, this.validationError, false);
        }

    }

    public MapValidator next() {
        validationError.ifPresent(e -> this.mapValidator.addValidationError(key, e));
        this.mapValidator.addValidatedValue(value);
        return this.mapValidator;
    }
}
