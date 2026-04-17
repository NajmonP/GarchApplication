package com.example.garchapplication.exception;

import com.example.garchapplication.model.enums.EntityType;
import org.springframework.http.HttpStatusCode;

public class WrongFileStructureException extends GarchApplicationException {
    public WrongFileStructureException(EntityType entityType, Throwable cause) {
        super(HttpStatusCode.valueOf(400), buildMessage(entityType), cause);
    }

    public WrongFileStructureException(String message) {
        super(HttpStatusCode.valueOf(400), message);
    }

    private static String buildMessage(EntityType entityType) {
        String errorMessage = "Špatná struktura souboru ";

        switch (entityType) {
            case CONFIGURATION -> errorMessage += "konfigurace.";
            case TIME_SERIES -> errorMessage += "časové řady.";
        }

        return errorMessage;
    }
}
