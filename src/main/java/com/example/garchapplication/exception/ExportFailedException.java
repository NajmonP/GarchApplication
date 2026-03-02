package com.example.garchapplication.exception;

import com.example.garchapplication.model.enums.EntityType;
import org.springframework.http.HttpStatusCode;

public class ExportFailedException extends GarchApplicationException {
    public ExportFailedException(EntityType entityType, Throwable cause) {
        super(HttpStatusCode.valueOf(500) ,buildMessage(entityType), cause);
    }

    private static String buildMessage(EntityType entityType) {
        return switch (entityType) {
            case CONFIGURATION -> "Při exportu konfigurace došlo k chybě.";
            case TIME_SERIES -> "Při exportu časové řady došlo k chybě.";
            default -> "Při exportu entity došlo k chybě.";
        };
    }
}
