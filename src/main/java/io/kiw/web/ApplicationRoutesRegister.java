package io.kiw.web;

import io.kiw.web.internal.RoutesRegister;

public interface ApplicationRoutesRegister<R> {
    R registerRoutes(RoutesRegister routesRegister);
}
