package io.kiw.template.web.infrastructure;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntryValidator<IN, T> {
    private final MapValidator<T> mapValidator;
    private final IN value;
    private final Optional<String> validationError;
    private final String key;

    EntryValidator(MapValidator<T> mapValidator, final String key, IN value, Optional<String> validationError) {
        this.mapValidator = mapValidator;
        this.value = value;
        this.key = key;
        this.validationError = validationError;
    }


    public EntryValidator<IN, T> validate(final Predicate<IN> check, final String errorMessage)
    {
        if(validationError.isEmpty())
        {
            boolean result = check.test(this.value);
            if(!result)
            {
                return new EntryValidator<>(mapValidator, this.key, this.value, Optional.of(errorMessage));
            }
        }

        return this;
    }

    public <OUT> EntryValidator<OUT, T> attemptMap(final Function<IN, OUT> mapper, final String errorMessage)
    {
        if(validationError.isEmpty())
        {
            try
            {
                final OUT apply = mapper.apply(this.value);
                return new EntryValidator<>(mapValidator, this.key, apply, this.validationError);
            }
            catch (Exception e)
            {
                return new EntryValidator<>(mapValidator, this.key, null, Optional.of(errorMessage));
            }
        }
        else
        {
            return new EntryValidator<>(mapValidator, this.key, null, this.validationError);
        }

    }

    public MapValidator<T> next() {
        validationError.ifPresent(e -> this.mapValidator.addValidationError(key, e));
        this.mapValidator.addValidatedValue(key, value);
        return this.mapValidator;
    }
}
