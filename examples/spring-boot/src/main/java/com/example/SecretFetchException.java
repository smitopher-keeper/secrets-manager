package com.example;

public class SecretFetchException extends RuntimeException {

  public SecretFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}
