package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;

public class ValidationResponse {
    public String name;
    public String email;
    public int age;
    public String city;
    public String page;
    public String userId;

    public ValidationResponse(String name, String email, int age, String city, String page, String userId) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.city = city;
        this.page = page;
        this.userId = userId;
    }
}
