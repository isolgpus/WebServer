package io.kiw.template.web.infrastructure;

public interface VertxJsonFilter {


    Flow handle(HttpControlStream<Void> e);

}
