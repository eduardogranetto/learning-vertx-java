package com.learning.learning_vertx_java;

import io.vertx.core.json.Json;

public class ErrorResponse {

  private final String code;
  private final String message;

  public ErrorResponse(String code, String message){
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String toJson(){
    return Json.encodePrettily(this);
  }

}
