package io.kiw.web;

import io.kiw.web.infrastructure.RoutesRegister;

public interface ApplicationRoutesRegister<R> {
    R registerRoutes(RoutesRegister routesRegister);
}
