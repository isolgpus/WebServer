package io.kiw.web.test;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;
import io.kiw.web.openapi.*;

public class MyApplicationState {
    private long longValue = 55;

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(int newLongValue) {
        this.longValue = newLongValue;
    }
}
