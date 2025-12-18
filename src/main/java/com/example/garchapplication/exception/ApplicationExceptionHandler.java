package com.example.garchapplication.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(MaxThresholdExceededException.class)
    public ResponseEntity<String> MaxThresholdExceeded(MaxThresholdExceededException ex) {
        String errorMessage = ex.getMessage() + " Součet vah: " + ex.getSum();
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(InvalidConstantVarianceException.class)
    public ResponseEntity<String> InvalidConstatVarianceException(InvalidConstantVarianceException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(InvalidLastValueException.class)
    public ResponseEntity<String> InvalidLastValueException(InvalidLastValueException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(MissingTimeSeriesException.class)
    public ResponseEntity<String> MissingTimeSeriesException(MissingTimeSeriesException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
