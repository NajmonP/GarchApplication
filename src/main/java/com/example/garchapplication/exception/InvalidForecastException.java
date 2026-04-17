package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class InvalidForecastException extends GarchApplicationException {
    public InvalidForecastException(int value) {
      super(HttpStatusCode.valueOf(422), "Předpověď musí být větší nebo rovna " + value);
    }
}
