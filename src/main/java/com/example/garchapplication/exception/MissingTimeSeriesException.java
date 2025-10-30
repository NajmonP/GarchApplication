package com.example.garchapplication.exception;

public class MissingTimeSeriesException extends GarchApplicationException {

    public MissingTimeSeriesException() {
        super("Pro výpočet je nutné vybrat časovou řadu ");
    }
}
