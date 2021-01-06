package com.learning.learning_vertx_java;

import com.learning.learning_vertx_java.domain.Customer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class HttpServerVerticle extends AbstractVerticle {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    router.route("/assets/*").handler(StaticHandler.create("assets"));

    router.get("/api/v1/customers").produces("application/json").handler(ctx -> {
      Customer customer = new Customer();
      customer.setName("Eduardo");
      ctx.response().end(Json.encodePrettily(customer));
    })
    ;

    router.post("/api/v1/customers").produces("application/json").handler(ctx ->{
      Customer customer = Json.decodeValue(
        ctx.getBodyAsString(), Customer.class
      );
      ctx.response().end(Json.encodePrettily(customer));
    })
    ;

    router.errorHandler(500, ctx -> {
      logger.error("", ctx.failure());
    });

    vertx.createHttpServer()
      .requestHandler(router).listen(config().getInteger("http.port", 8888), http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
