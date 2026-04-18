package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class InvalidCredentialsException extends GarchApplicationException {
    public InvalidCredentialsException(String message) {
        super(HttpStatusCode.valueOf(409), message);
    }
}
