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
        EntryValidator<String> entryValidator = new EntryValidator<>(this, key, valueRetriever.apply(key), Optional.empty(), true);
        return entryValidator.validate(Objects::nonNull, "is required");
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
}
