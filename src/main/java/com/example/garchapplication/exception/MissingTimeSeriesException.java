package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;

public class MissingTimeSeriesException extends GarchApplicationException {

    public MissingTimeSeriesException() {
        super(HttpStatusCode.valueOf(404), "Pro výpočet je nutné vybrat časovou řadu ");
    }
}
