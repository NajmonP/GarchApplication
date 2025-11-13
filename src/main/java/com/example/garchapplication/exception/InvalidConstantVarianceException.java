package com.example.garchapplication.exception;

public class InvalidConstantVarianceException extends GarchApplicationException {
    public InvalidConstantVarianceException() {
        super("Konstantní rozptyl musí být > 0");
    }
}
