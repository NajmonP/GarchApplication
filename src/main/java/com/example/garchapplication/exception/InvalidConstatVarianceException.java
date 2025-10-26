package com.example.garchapplication.exception;

public class InvalidConstatVarianceException extends GarchApplicationException {
    public InvalidConstatVarianceException() {
        super("Konstantní rozptyl musí být > 0");
    }
}
