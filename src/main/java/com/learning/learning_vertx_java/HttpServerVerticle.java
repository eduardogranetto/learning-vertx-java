package com.learning.learning_vertx_java;

import com.learning.learning_vertx_java.domain.Customer;
import com.learning.learning_vertx_java.domain.CustomerJdbcRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Optional;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.vertx.core.json.Json.encodePrettily;

public class HttpServerVerticle extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private CustomerJdbcRepository customerJdbcRepository;

  @Override
  public void start(Promise<Void> startPromise) {
    this.customerJdbcRepository = new CustomerJdbcRepository(vertx);
    Router router = Router.router(vertx);

    router.route("/assets/*").handler(StaticHandler.create("assets"));
    router.route().handler(BodyHandler.create());

    router.get("/api/v1/customers").produces("application/json").handler(ctx -> {
      customerJdbcRepository.findAll().onComplete(ar -> {
        if (ar.failed()) {
          ctx.fail(ar.cause());
        } else {
          ctx.response().end(ar.result().encode());
        }
      });
    })
    ;

    router.post("/api/v1/customers").produces("application/json").handler(ctx -> {
      Customer customer = Json.decodeValue(
        ctx.getBodyAsString(), Customer.class
      );
      customer.setId(UUID.randomUUID());
      customerJdbcRepository.create(customer).onComplete(ar -> {
        if (ar.failed()) {
          ctx.fail(ar.cause());
        } else {
          ctx.response().end(encodePrettily(customer));
        }
      });

    })
    ;

    router.get("/api/v1/customers/:id").produces("application/json").handler(ctx -> {
      UUID id = UUID.fromString(ctx.pathParam("id"));
      customerJdbcRepository.findById(id).onComplete(ar -> {
        if (ar.failed()) {
          ctx.fail(ar.cause());
        } else {
          ctx.response().end(ar.result().map(Json::encodePrettily).orElseGet(() -> {
                ctx.response().setStatusCode(NOT_FOUND.code());
                return new ErrorResponse("CUSTOMER_NOT_FOUND", "Customer not found").toJson();
              }
            )
          );
        }
      });
    })
    ;

    router.errorHandler(500, ctx -> logger.error("", ctx.failure()));
    Integer port = config().getInteger("http.port", 8888);
    vertx.createHttpServer()
      .requestHandler(router).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        logger.info("HTTP server started on port " + port);
      } else {
        startPromise.fail(http.cause());
      }
    })
    ;
  }
}
