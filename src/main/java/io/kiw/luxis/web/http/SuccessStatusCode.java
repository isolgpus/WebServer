package io.kiw.luxis.web.http;

import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.validation.*;

public enum SuccessStatusCode {
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NON_AUTHORITATIVE_INFORMATION(203),
    NO_CONTENT(204),
    RESET_CONTENT(205),
    PARTIAL_CONTENT(206),
    MULTI_STATUS(207),
    ALREADY_REPORTED(208),
    IM_USED(226);

    private final int code;

    SuccessStatusCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
