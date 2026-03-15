package io.kiw.web.infrastructure.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kiw.web.infrastructure.Method;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenApiSpecGenerator {
    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");

    private final OpenApiCollector collector;
    private final ObjectMapper objectMapper;
    private final JsonSchemaGenerator schemaGenerator;
    private String title = "API";
    private String version = "1.0.0";
    private String description;

    public OpenApiSpecGenerator(OpenApiCollector collector, ObjectMapper objectMapper) {
        this.collector = collector;
        this.objectMapper = objectMapper;
        this.schemaGenerator = new JsonSchemaGenerator(objectMapper);
    }

    public OpenApiSpecGenerator title(String title) {
        this.title = title;
        return this;
    }

    public OpenApiSpecGenerator version(String version) {
        this.version = version;
        return this;
    }

    public OpenApiSpecGenerator description(String description) {
        this.description = description;
        return this;
    }

    public ObjectNode generate() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("openapi", "3.0.3");

        ObjectNode info = root.putObject("info");
        info.put("title", title);
        info.put("version", version);
        if (description != null) {
            info.put("description", description);
        }

        ObjectNode paths = root.putObject("paths");
        Map<String, Map<String, RouteDescriptor>> groupedByPath = groupRoutes();

        for (var pathEntry : groupedByPath.entrySet()) {
            String openApiPath = convertPath(pathEntry.getKey());
            ObjectNode pathItem = paths.putObject(openApiPath);

            for (var methodEntry : pathEntry.getValue().entrySet()) {
                String httpMethod = methodEntry.getKey().toLowerCase();
                RouteDescriptor desc = methodEntry.getValue();
                ObjectNode operation = pathItem.putObject(httpMethod);
                buildOperation(operation, desc, pathEntry.getKey());
            }
        }

        return root;
    }

    private Map<String, Map<String, RouteDescriptor>> groupRoutes() {
        Map<String, Map<String, RouteDescriptor>> grouped = new LinkedHashMap<>();
        for (RouteDescriptor route : collector.getRoutes()) {
            if (route.metadata != null && route.metadata.hidden) {
                continue;
            }
            if (route.kind == RouteDescriptor.RouteKind.FILTER) {
                continue;
            }
            String methodName = route.method != null ? route.method.name() : "GET";
            grouped.computeIfAbsent(route.path, k -> new LinkedHashMap<>())
                .put(methodName, route);
        }
        return grouped;
    }

    private void buildOperation(ObjectNode operation, RouteDescriptor desc, String rawPath) {
        if (desc.metadata != null) {
            if (desc.metadata.summary != null) {
                operation.put("summary", desc.metadata.summary);
            }
            if (desc.metadata.description != null) {
                operation.put("description", desc.metadata.description);
            }
            if (!desc.metadata.tags.isEmpty()) {
                ArrayNode tags = operation.putArray("tags");
                desc.metadata.tags.forEach(tags::add);
            }
        }

        operation.put("operationId", generateOperationId(desc.method, rawPath));

        ArrayNode parameters = buildPathParameters(rawPath, desc.metadata);
        if (parameters.size() > 0) {
            operation.set("parameters", parameters);
        }

        if (desc.method != null && desc.method.canHaveABody() && desc.inputType != null) {
            ObjectNode requestBody = operation.putObject("requestBody");
            requestBody.put("required", true);
            ObjectNode content = requestBody.putObject("content");
            ObjectNode mediaType = content.putObject(desc.consumes);
            ObjectNode schema = schemaGenerator.generateSchema(desc.inputType);
            if (schema != null) {
                mediaType.set("schema", schema);
            }
        } else if (desc.kind == RouteDescriptor.RouteKind.UPLOAD) {
            ObjectNode requestBody = operation.putObject("requestBody");
            requestBody.put("required", true);
            ObjectNode content = requestBody.putObject("content");
            ObjectNode mediaType = content.putObject("multipart/form-data");
            ObjectNode schema = objectMapper.createObjectNode().put("type", "object");
            schema.putObject("additionalProperties").put("type", "string").put("format", "binary");
            mediaType.set("schema", schema);
        }

        ObjectNode responses = operation.putObject("responses");
        ObjectNode successResponse = responses.putObject("200");
        String responseDesc = (desc.metadata != null && desc.metadata.responseDescription != null)
            ? desc.metadata.responseDescription : "Successful response";
        successResponse.put("description", responseDesc);

        if (desc.outputType != null && desc.outputType != Void.class) {
            ObjectNode respContent = successResponse.putObject("content");
            ObjectNode respMediaType = respContent.putObject(desc.produces);
            ObjectNode schema = schemaGenerator.generateSchema(desc.outputType);
            if (schema != null) {
                respMediaType.set("schema", schema);
            }
        }
    }

    private String convertPath(String path) {
        return PATH_PARAM_PATTERN.matcher(path).replaceAll("{$1}");
    }

    private String generateOperationId(Method method, String rawPath) {
        String methodStr = method != null ? method.name().toLowerCase() : "get";
        String pathPart = rawPath.replaceAll("[/:{}]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
        return methodStr + "_" + pathPart;
    }

    private ArrayNode buildPathParameters(String path, OpenApiMetadata metadata) {
        ArrayNode params = objectMapper.createArrayNode();
        Matcher m = PATH_PARAM_PATTERN.matcher(path);
        while (m.find()) {
            String paramName = m.group(1);
            ObjectNode param = objectMapper.createObjectNode();
            param.put("name", paramName);
            param.put("in", "path");
            param.put("required", true);
            param.putObject("schema").put("type", "string");
            if (metadata != null && metadata.parameterDescriptions.containsKey(paramName)) {
                param.put("description", metadata.parameterDescriptions.get(paramName));
            }
            params.add(param);
        }
        return params;
    }
}
