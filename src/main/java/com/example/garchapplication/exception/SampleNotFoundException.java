package com.example.garchapplication.exception;

import com.example.garchapplication.model.enums.EntityType;
import org.springframework.http.HttpStatusCode;

public class SampleNotFoundException extends GarchApplicationException {
    public SampleNotFoundException(EntityType entityType) {
        super(HttpStatusCode.valueOf(500), buildMessage(entityType));
    }

    private static String buildMessage(EntityType entityType) {
        return switch (entityType) {
            case CONFIGURATION -> "Ukázková konfigurace není dostupná.";
            case TIME_SERIES -> "Ukázková časová řada není dostupná.";
            default -> "Ukázkový soubor není dostupný.";
        };
    }
}
