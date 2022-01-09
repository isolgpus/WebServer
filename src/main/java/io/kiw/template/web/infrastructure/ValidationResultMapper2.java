package io.kiw.template.web.infrastructure;

public interface ValidationResultMapper2<IN1, IN2, OUT> {
    OUT map(IN1 in1, IN2 in2);
}
