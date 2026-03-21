package io.kiw.luxis.web.openapi;

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

    OpenApiMetadata(final String summary, final String description, final List<String> tags,
                    final Map<String, String> parameterDescriptions, final String responseDescription,
                    final boolean hidden) {
        this.summary = summary;
        this.description = description;
        this.tags = Collections.unmodifiableList(tags);
        this.parameterDescriptions = Collections.unmodifiableMap(parameterDescriptions);
        this.responseDescription = responseDescription;
        this.hidden = hidden;
    }
}
