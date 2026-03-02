package com.example.garchapplication.exception;

import com.example.garchapplication.model.enums.EntityType;
import org.springframework.http.HttpStatusCode;

public class DuplicateNameException extends GarchApplicationException {
    public DuplicateNameException(EntityType entityType, Throwable ex) {
        super(HttpStatusCode.valueOf(409), buildMessage(entityType), ex);
    }

    private static String buildMessage(EntityType entityType) {
        String errorMessage = "";

        switch (entityType) {
            case CONFIGURATION -> errorMessage = "V systému už konfiguraci s tímto názvem máte.";
            case GARCH_MODEL -> errorMessage = "Soubor s konfigurací obsahuje duplicitní názvy GARCH modelů.";
            case TIME_SERIES -> errorMessage = "V systému už časovou řadu s tímto názvem máte.";
        }

        return errorMessage;
    }
}
