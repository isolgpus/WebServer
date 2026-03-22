package io.kiw.luxis.web.test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import io.kiw.luxis.web.internal.JacksonUtil;

public final class TestHelper {
    private TestHelper() { }

    public static final ObjectMapper MAPPER = JacksonUtil.createMapper();

    public static ObjectNode json() {
        return MAPPER.createObjectNode();
    }

    public static String file(final String fileContents) {
        return fileContents;
    }
}
