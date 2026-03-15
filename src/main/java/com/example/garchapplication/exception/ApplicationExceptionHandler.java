package com.example.garchapplication.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(GarchApplicationException.class)
    public ResponseEntity<String> handleException(GarchApplicationException ex) {
        return ResponseEntity.status(ex.getHttpStatusCode()).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> Exception(Exception ex) {
        return ResponseEntity.status(HttpStatusCode.valueOf(500)).body("Došlo k nečekané chybě");
    }
}
