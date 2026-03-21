package io.kiw.web.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.http.HttpResult;
import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.handler.VertxJsonRoute;

public class OpenApiRoute extends VertxJsonRoute<Void, ObjectNode, Object> {
    private final OpenApiCollector collector;
    private final ObjectMapper objectMapper;
    private final String title;
    private final String version;
    private final String description;
    private volatile ObjectNode cachedSpec;

    public OpenApiRoute(OpenApiCollector collector, ObjectMapper objectMapper,
                        String title, String version, String description) {
        this.collector = collector;
        this.objectMapper = objectMapper;
        this.title = title;
        this.version = version;
        this.description = description;
    }

    @Override
    public RequestPipeline<ObjectNode> handle(HttpStream<Void, Object> e) {
        return e.complete(ctx -> {
            if (cachedSpec == null) {
                OpenApiSpecGenerator generator = new OpenApiSpecGenerator(collector, objectMapper);
                generator.title(title).version(version).description(description);
                cachedSpec = generator.generate();
            }
            return HttpResult.success(cachedSpec);
        });
    }
}
