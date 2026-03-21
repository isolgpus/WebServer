package io.kiw.luxis.web.openapi;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.pipeline.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonSchemaGenerator {
    private final ObjectMapper objectMapper;

    public JsonSchemaGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode generateSchema(Type type) {
        if (type == null || type == Void.class || type == void.class) {
            return null;
        }
        return generateSchemaForType(type);
    }

    private ObjectNode generateSchemaForType(Type type) {
        if (type instanceof Class<?> cls) {
            return generateSchemaForClass(cls);
        } else if (type instanceof ParameterizedType pt) {
            return generateSchemaForParameterized(pt);
        }
        return objectMapper.createObjectNode().put("type", "object");
    }

    private ObjectNode generateSchemaForClass(Class<?> cls) {
        if (cls == int.class || cls == Integer.class) {
            return objectMapper.createObjectNode().put("type", "integer");
        }
        if (cls == long.class || cls == Long.class) {
            return objectMapper.createObjectNode().put("type", "integer").put("format", "int64");
        }
        if (cls == double.class || cls == Double.class) {
            return objectMapper.createObjectNode().put("type", "number").put("format", "double");
        }
        if (cls == float.class || cls == Float.class) {
            return objectMapper.createObjectNode().put("type", "number").put("format", "float");
        }
        if (cls == boolean.class || cls == Boolean.class) {
            return objectMapper.createObjectNode().put("type", "boolean");
        }
        if (cls == String.class) {
            return objectMapper.createObjectNode().put("type", "string");
        }

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        ArrayNode required = objectMapper.createArrayNode();

        for (Field field : cls.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            properties.set(field.getName(), generateSchemaForType(field.getGenericType()));
            if (field.getType().isPrimitive()) {
                required.add(field.getName());
            }
        }
        if (required.size() > 0) {
            schema.set("required", required);
        }
        return schema;
    }

    private ObjectNode generateSchemaForParameterized(ParameterizedType pt) {
        Class<?> raw = (Class<?>) pt.getRawType();
        if (List.class.isAssignableFrom(raw) || Collection.class.isAssignableFrom(raw)) {
            ObjectNode schema = objectMapper.createObjectNode().put("type", "array");
            schema.set("items", generateSchemaForType(pt.getActualTypeArguments()[0]));
            return schema;
        }
        if (Map.class.isAssignableFrom(raw)) {
            ObjectNode schema = objectMapper.createObjectNode().put("type", "object");
            schema.set("additionalProperties", generateSchemaForType(pt.getActualTypeArguments()[1]));
            return schema;
        }
        return generateSchemaForClass(raw);
    }
}
