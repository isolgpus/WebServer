package io.kiw.luxis.web.openapi;

import io.kiw.luxis.web.http.Method;

import java.lang.reflect.Type;

public class RouteDescriptor {
    public final String path;
    public final Method method;
    public final Type inputType;
    public final Type outputType;
    public final String consumes;
    public final String produces;
    public final RouteKind kind;
    public final OpenApiMetadata metadata;

    public RouteDescriptor(String path, Method method, Type inputType, Type outputType,
                           String consumes, String produces, RouteKind kind,
                           OpenApiMetadata metadata) {
        this.path = path;
        this.method = method;
        this.inputType = inputType;
        this.outputType = outputType;
        this.consumes = consumes;
        this.produces = produces;
        this.kind = kind;
        this.metadata = metadata;
    }

    public enum RouteKind {
        JSON, UPLOAD, DOWNLOAD, WEBSOCKET, FILTER
    }
}
