package io.kiw.web;

import io.kiw.web.internal.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.cors.*;
import io.kiw.web.jwt.*;
import io.kiw.web.openapi.*;

import java.util.function.BiConsumer;

public interface WebServer<APP> {
    <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer);
    void stop();
}
