package io.kiw.template.web.infrastructure;

import java.util.*;
import java.util.function.Function;

public class MapValidator {
    private final Function<String, String> valueRetriever;
    private final List<Object> validatedValues = new ArrayList<>();
    private final Map<String, String> validationErrors = new LinkedHashMap<>();

    public MapValidator(Function<String, String> valueRetriever) {
        this.valueRetriever = valueRetriever;
    }

    public EntryValidator<String> required(String key) {
        EntryValidator<String> entryValidator = new EntryValidator<>(this, key, valueRetriever.apply(key), Optional.empty());
        return entryValidator.validate(Objects::nonNull, "is required");
    }

    public EntryValidator<String> optional(String key, String defaultValue) {
        String value = valueRetriever.apply(key);
        if(value == null)
        {
            value = defaultValue;
        }
        return new EntryValidator<>(this, key, value, Optional.empty());
    }

    public OptionalEntryValidator<String> optional(String key) {
        Optional<String> value = Optional.ofNullable(valueRetriever.apply(key));

        return new OptionalEntryValidator<>(this, key, value, Optional.empty(), true);
    }

    public void addValidationError(String key, String validationError) {
        this.validationErrors.put(key, validationError);
    }

    public void addValidatedValue(Object value) {
        this.validatedValues.add(value);
    }

    public <IN, OUT> HttpResult<OUT> toHttpResult(ValidationResultMapper<IN, OUT> validationResultMapper) {
        if(validationErrors.size() > 0)
        {
            return HttpResult.error(400, new MessageResponse(""));
        }
        return HttpResult.success(validationResultMapper.map((IN)this.validatedValues.get(0)));
    }

    public <IN1, IN2, OUT> HttpResult<OUT> toHttpResult(ValidationResultMapper2<IN1, IN2, OUT> validationResultMapper) {

        if(validationErrors.size() > 0)
        {
            return HttpResult.error(400, new MessageResponse("There were unexpected validation errors", this.validationErrors));
        }
        return HttpResult.success(validationResultMapper.map((IN1)this.validatedValues.get(0), (IN2)this.validatedValues.get(1)));
    }

    public <IN1, IN2, IN3, OUT> HttpResult<OUT> toHttpResult(ValidationResultMapper3<IN1, IN2, IN3, OUT> validationResultMapper) {

        if(validationErrors.size() > 0)
        {
            return HttpResult.error(400, new MessageResponse("There were unexpected validation errors", this.validationErrors));
        }
        return HttpResult.success(validationResultMapper.map((IN1)this.validatedValues.get(0), (IN2)this.validatedValues.get(1), (IN3)this.validatedValues.get(2)));
    }

    public <IN1, IN2, IN3, IN4, OUT> HttpResult<OUT> toHttpResult(ValidationResultMapper4<IN1, IN2, IN3, IN4, OUT> validationResultMapper) {

        if(validationErrors.size() > 0)
        {
            return HttpResult.error(400, new MessageResponse("There were unexpected validation errors", this.validationErrors));
        }
        return HttpResult.success(validationResultMapper.map((IN1)this.validatedValues.get(0), (IN2)this.validatedValues.get(1), (IN3)this.validatedValues.get(2), (IN4)this.validatedValues.get(3)));
    }

    public <IN1, IN2, IN3, IN4, IN5, OUT> HttpResult<OUT> toHttpResult(ValidationResultMapper5<IN1, IN2, IN3, IN4, IN5, OUT> validationResultMapper) {

        if(validationErrors.size() > 0)
        {
            return HttpResult.error(400, new MessageResponse("There were unexpected validation errors", this.validationErrors));
        }
        return HttpResult.success(validationResultMapper.map(
                (IN1)this.validatedValues.get(0),
                (IN2)this.validatedValues.get(1),
                (IN3)this.validatedValues.get(2),
                (IN4)this.validatedValues.get(3),
                (IN5)this.validatedValues.get(4))
        );
    }

    public <IN1, IN2, IN3, IN4, IN5, IN6, OUT> HttpResult<OUT> toHttpResult(ValidationResultMapper6<IN1, IN2, IN3, IN4, IN5, IN6, OUT> validationResultMapper) {

        if(validationErrors.size() > 0)
        {
            return HttpResult.error(400, new MessageResponse("There were unexpected validation errors", this.validationErrors));
        }
        return HttpResult.success(validationResultMapper.map(
                        (IN1)this.validatedValues.get(0),
                        (IN2)this.validatedValues.get(1),
                        (IN3)this.validatedValues.get(2),
                        (IN4)this.validatedValues.get(3),
                        (IN5)this.validatedValues.get(4),
                        (IN6)this.validatedValues.get(5))
        );
    }
}
