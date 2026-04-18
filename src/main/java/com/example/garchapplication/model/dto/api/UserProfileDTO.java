package com.example.garchapplication.model.dto.api;

import com.example.garchapplication.model.enums.RoleType;

public record UserProfileDTO(
        Long id,
        String username,
        String email,
        RoleType role
) {}
