package com.learning.learning_vertx_java;

import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args) {
    Vertx.vertx()
      .deployVerticle(new HttpServerVerticle())
      ;
  }

}
