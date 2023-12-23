package io.kiw.web.infrastructure;

public interface VertxJsonFilter<APP>{

    Flow handle(HttpResponseStream<Void, APP> e);

}
