package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class InvalidConstantVarianceException extends GarchApplicationException {
    public InvalidConstantVarianceException() {
        super(HttpStatusCode.valueOf(422), "konstantní člen musí být > 0");
    }
}
