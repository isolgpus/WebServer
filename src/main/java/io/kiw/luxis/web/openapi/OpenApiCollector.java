package io.kiw.luxis.web.openapi;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.pipeline.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenApiCollector {
    private final List<RouteDescriptor> routes = new ArrayList<>();

    public void addRoute(RouteDescriptor descriptor) {
        routes.add(descriptor);
    }

    public List<RouteDescriptor> getRoutes() {
        return Collections.unmodifiableList(routes);
    }
}
