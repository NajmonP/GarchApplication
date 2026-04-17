package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class InvalidLastValueException extends GarchApplicationException {
    public InvalidLastValueException(double value) {
        super(HttpStatusCode.valueOf(422), "Hodnota minulé hodnoty musí být ≥ " + value);
    }
}
