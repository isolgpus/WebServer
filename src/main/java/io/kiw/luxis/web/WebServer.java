package io.kiw.luxis.web;

import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.cors.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.openapi.*;

import java.util.function.BiConsumer;

public interface WebServer<APP> {
    <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer);
    void stop();
}
