package io.kiw.result;

public interface ValidationLogic<I> {
    boolean validate(I value);
}
