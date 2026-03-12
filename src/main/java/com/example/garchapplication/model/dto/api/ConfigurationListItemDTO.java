package com.example.garchapplication.model.dto.api;


import java.time.Instant;

public record ConfigurationListItemDTO(
        long id,
        String name,
        String username,
        Instant created
) {
}
