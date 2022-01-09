package io.kiw.template.web.infrastructure;

public interface ValidationResultMapper<IN, OUT> {
    OUT map(IN t1);
}
