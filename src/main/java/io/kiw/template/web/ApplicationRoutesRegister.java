package io.kiw.template.web;

import io.kiw.template.web.infrastructure.RoutesRegister;

public interface ApplicationRoutesRegister<R> {
    R registerRoutes(RoutesRegister routesRegister);
}
