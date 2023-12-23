package io.kiw.result;

public interface Mappable2With<I, O> {
    O map(I input);

    void validate(Validator validator);
}
