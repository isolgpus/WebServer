package io.kiw.luxis.web.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class TestHelper {
    private TestHelper() { }

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static ObjectNode json() {
        return MAPPER.createObjectNode();
    }

    public static String file(final String fileContents) {
        return fileContents;
    }
}
