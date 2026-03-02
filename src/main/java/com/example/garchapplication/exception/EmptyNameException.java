package com.example.garchapplication.exception;

import com.example.garchapplication.model.enums.EntityType;
import org.springframework.http.HttpStatusCode;

public class EmptyNameException extends GarchApplicationException {
    public EmptyNameException(EntityType entityType) {
        super(HttpStatusCode.valueOf(400), buildMessage(entityType));
    }

    private static String buildMessage(EntityType entityType) {
        String errorMessage = "V přiloženém souboru chybí název ";

        switch (entityType) {
            case CONFIGURATION -> errorMessage += "konfigurace.";
            case GARCH_MODEL -> errorMessage += "GARCH modelu.";
            case TIME_SERIES -> errorMessage += "časové řady.";
        }

        return errorMessage;
    }
}
