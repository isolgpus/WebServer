package io.kiw.template.web.infrastructure;

public interface VertxJsonFilter<APP>{

    Flow handle(HttpControlStream<Void, APP> e);

}
