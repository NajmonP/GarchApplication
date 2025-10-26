package com.example.garchapplication.exception;

public class MaxThresholdExceededException extends GarchApplicationException {
    private final double sum;

    public MaxThresholdExceededException(double sum) {
        super("Součet vah musí být < 1");
        this.sum = sum;
    }

    public double getSum() {
        return sum;
    }
}
