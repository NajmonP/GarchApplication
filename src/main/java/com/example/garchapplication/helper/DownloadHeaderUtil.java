package com.example.garchapplication.helper;

import org.springframework.http.ContentDisposition;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

public final class DownloadHeaderUtil {

    private static final String DEFAULT_NAME = "Download";

    public static ContentDisposition createExcelAttachment(String rawName) {
        String base = rawName.trim();

        // zakázané znaky Windows
        base = base.replaceAll("[\\\\/:*?\"<>|]", "_");

        String utf8Filename = base + ".xlsx";

        // ASCII fallback bez diakritiky
        String asciiBase = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9._-]", "_");

        if (asciiBase.isBlank()) {
            asciiBase = DEFAULT_NAME;
        }

        String asciiFilename = asciiBase + ".xlsx";

        return ContentDisposition.attachment()
                .filename(asciiFilename)
                .filename(utf8Filename, StandardCharsets.UTF_8)
                .build();
    }
}

