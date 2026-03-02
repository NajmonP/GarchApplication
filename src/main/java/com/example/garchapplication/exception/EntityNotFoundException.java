package com.example.garchapplication.exception;

import com.example.garchapplication.model.enums.EntityType;
import org.springframework.http.HttpStatusCode;

public class EntityNotFoundException extends GarchApplicationException {
    public EntityNotFoundException(long id, EntityType entityType) {
        super(HttpStatusCode.valueOf(404), buildMessage(id, entityType));
    }

    private static String buildMessage(long id, EntityType entityType) {
        return switch (entityType) {
            case CONFIGURATION -> "Konfigurace s id={" + id + "} nebyla nalezena.";
            case GARCH_MODEL -> "Garch model s id={" + id + "} nebyl nalezen.";
            case TIME_SERIES -> "Časová řada s id={" + id + "} nebyla nalezena.";
            default -> "Entita s id={" + id + "} nebyla nalezena.";
        };
    }
}
