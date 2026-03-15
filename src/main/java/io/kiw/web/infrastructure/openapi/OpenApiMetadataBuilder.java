package io.kiw.web.infrastructure.openapi;

import io.kiw.web.test.handler.RouteConfig;
import io.kiw.web.test.handler.RouteConfigBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenApiMetadataBuilder {
    private final RouteConfigBuilder parent;
    private String summary;
    private String description;
    private final List<String> tags = new ArrayList<>();
    private final Map<String, String> parameterDescriptions = new LinkedHashMap<>();
    private String responseDescription;
    private boolean hidden = false;

    public OpenApiMetadataBuilder(RouteConfigBuilder parent) {
        this.parent = parent;
    }

    public OpenApiMetadataBuilder summary(String summary) {
        this.summary = summary;
        return this;
    }

    public OpenApiMetadataBuilder description(String description) {
        this.description = description;
        return this;
    }

    public OpenApiMetadataBuilder tag(String tag) {
        this.tags.add(tag);
        return this;
    }

    public OpenApiMetadataBuilder paramDescription(String param, String description) {
        this.parameterDescriptions.put(param, description);
        return this;
    }

    public OpenApiMetadataBuilder responseDescription(String description) {
        this.responseDescription = description;
        return this;
    }

    public OpenApiMetadataBuilder hidden() {
        this.hidden = true;
        return this;
    }

    public RouteConfigBuilder done() {
        return parent;
    }

    public RouteConfig build() {
        return parent.build();
    }

    public OpenApiMetadata buildMetadata() {
        return new OpenApiMetadata(summary, description, tags, parameterDescriptions,
            responseDescription, hidden);
    }
}
