package com.example.garchapplication.model.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Email String email
) {
}
