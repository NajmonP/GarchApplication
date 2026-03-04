package com.example.garchapplication.model.dto.api;

import java.time.Instant;

public record TimeSeriesListItemDTO(
        long id,
        String name,
        Instant created,
        String username,
        String visibility
) {}
