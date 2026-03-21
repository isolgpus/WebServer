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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestHelper {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static ObjectNode json() {
        return MAPPER.createObjectNode();
    }

    public static String file(String fileContents) {
        return fileContents;
    }
}
