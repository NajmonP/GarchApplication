package com.example.garchapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidGarchParamsException extends GarchApplicationException {
    private final double lastVariance;
    private final double lastShock;

    public InvalidGarchParamsException(double lastVariance, double lastShock) {
        super("Součet vah musí být < 1");
        this.lastVariance = lastVariance;
        this.lastShock = lastShock;
    }

    public double getLastVariance() {
        return lastVariance;
    }

    public double getLastShock() {
        return lastShock;
    }
}
