package io.kiw.web.infrastructure;

public interface VertxJsonFilter<APP>{

    RequestPipeline handle(HttpResponseStream<Void, APP> e);

}
