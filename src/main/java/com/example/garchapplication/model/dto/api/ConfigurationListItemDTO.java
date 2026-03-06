package com.example.garchapplication.model.dto.api;


import java.time.Instant;

public record ConfigurationListItemDTO(
        long id,
        String name,
        Instant created
) {
}
