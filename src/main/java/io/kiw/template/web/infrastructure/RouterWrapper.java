package io.kiw.template.web.infrastructure;

public interface RouterWrapper {
    void route(String path, Method method, String consumes, String provides, ContextHandler contextHandler);
}
