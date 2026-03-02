package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class InvalidLastValueException extends GarchApplicationException {
    public InvalidLastValueException() {
        super(HttpStatusCode.valueOf(422), "Hodnota minulé hodnoty musí být ≥ 0");
    }
}
