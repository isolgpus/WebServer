package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;

public class ValidationRequest {
    public String name;
    public String email;
    public Integer age;
    public AddressBody address;

    public static class AddressBody {
        public String city;
        public String zip;
    }
}
