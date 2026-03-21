package io.kiw.luxis.web.openapi;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.pipeline.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OpenApiMetadata {
    public final String summary;
    public final String description;
    public final List<String> tags;
    public final Map<String, String> parameterDescriptions;
    public final String responseDescription;
    public final boolean hidden;

    OpenApiMetadata(String summary, String description, List<String> tags,
                    Map<String, String> parameterDescriptions, String responseDescription,
                    boolean hidden) {
        this.summary = summary;
        this.description = description;
        this.tags = Collections.unmodifiableList(tags);
        this.parameterDescriptions = Collections.unmodifiableMap(parameterDescriptions);
        this.responseDescription = responseDescription;
        this.hidden = hidden;
    }
}
