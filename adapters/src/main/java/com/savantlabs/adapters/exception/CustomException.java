package com.savantlabs.adapters.exception;

import org.springframework.http.HttpStatusCode;

public class CustomException extends RuntimeException {
    private final HttpStatusCode status;

    public CustomException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}