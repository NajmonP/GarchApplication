package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class InvalidDateRangeException extends GarchApplicationException {
    public InvalidDateRangeException() {
        super(HttpStatusCode.valueOf(422), "Datum \"Od\" musí být menší nebo rovno datu \"Do\"");
    }
}
