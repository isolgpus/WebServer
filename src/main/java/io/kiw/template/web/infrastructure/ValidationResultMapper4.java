package io.kiw.template.web.infrastructure;

public interface ValidationResultMapper4<IN1, IN2, IN3, IN4, OUT> {
    OUT map(IN1 in1, IN2 in2, IN3 in3, IN4 in4);
}
