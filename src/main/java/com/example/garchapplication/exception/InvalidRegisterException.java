package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class InvalidRegisterException extends GarchApplicationException {
    public InvalidRegisterException(String message) {
        super(HttpStatusCode.valueOf(409), message);
    }
}
