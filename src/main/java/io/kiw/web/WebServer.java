package io.kiw.web;

import java.util.function.BiConsumer;

public interface WebServer<APP> {
    <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer);
    void stop();
}
