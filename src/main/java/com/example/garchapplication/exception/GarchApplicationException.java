package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public abstract class GarchApplicationException extends RuntimeException {
    private final HttpStatusCode httpStatusCode;

    public GarchApplicationException(HttpStatusCode httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public GarchApplicationException(HttpStatusCode httpStatusCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }
}
