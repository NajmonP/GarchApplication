package com.example.garchapplication.exception;

public class InvalidLastValueException extends RuntimeException {
    public InvalidLastValueException() {
        super("Hodnota minulé hodnoty musí být ≥ 0");
    }
}
