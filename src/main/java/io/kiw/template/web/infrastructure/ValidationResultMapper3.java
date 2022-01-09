package io.kiw.template.web.infrastructure;

public interface ValidationResultMapper3<IN1, IN2, IN3, OUT> {
    OUT map(IN1 in1, IN2 in2, IN3 in3);
}
