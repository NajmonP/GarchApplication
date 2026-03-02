package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class MaxThresholdExceededException extends GarchApplicationException {

    public MaxThresholdExceededException(double sum) {
        super(HttpStatusCode.valueOf(422), "Součet vah musí být < 1. Součet vah = " + sum);
    }
}
