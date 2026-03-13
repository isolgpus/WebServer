package io.kiw.web.test;

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
