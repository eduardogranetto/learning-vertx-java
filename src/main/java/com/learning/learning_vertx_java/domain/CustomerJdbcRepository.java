package com.learning.learning_vertx_java.domain;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.fromString;

public class CustomerJdbcRepository {

  private final JDBCClient sqlClient;

  public CustomerJdbcRepository(final Vertx vertx) {
    JsonObject config = new JsonObject();
    config.put("url", "jdbc:postgresql://localhost:5432/learning_vertx_java");
    config.put("driver_class", "org.postgresql.Driver");
    config.put("user", "learning_vertx_java");
    config.put("password", "learning_vertx_java"); //For production manage password in config file or through env
    this.sqlClient = JDBCClient.createShared(vertx, config);
  }

  public Future<JsonArray> findAll() {
      final Promise<JsonArray> all = Promise.promise();
      sqlClient.query("select * from customers", ar -> {
        if(ar.failed()) {
          all.fail(ar.cause());
        }else{
          all.complete(new JsonArray(ar.result().getRows()));
        }
      });
      return all.future();
  }

  public Future<Customer> create(Customer customer) {
    final Promise<Customer> promise = Promise.promise();
    final JsonArray params = new JsonArray().add(customer.getId()).add(customer.getName());
    sqlClient.updateWithParams("insert into customers(id, name) values (?, ?)", params, ar -> {
      if(ar.failed()){
        promise.fail(ar.cause());
      }else{
        promise.complete(customer);
      }
    });
    return promise.future();
  }

  public Future<Optional<Customer>> findById(UUID id) {
    final Promise<Optional<Customer>> promise = Promise.promise();
    final JsonArray params = new JsonArray().add(id);
    sqlClient.queryWithParams("select id, name from customers where id = ?", params, ar -> {
      if(ar.failed()){
        promise.fail(ar.cause());
      }else{
        promise.complete(ar.result().getRows().stream().findFirst().map(json -> {
          Customer customer = new Customer();
          customer.setId(fromString(json.getString("id")));
          customer.setName(json.getString("name"));
          return customer;
        }));
      }
    });
    return promise.future();
  }
}
