package com.example.garchapplication.model.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(min = 8, max = 72) String currentPassword,
        @NotBlank @Size(min = 8, max = 72) String newPassword,
        @NotBlank @Size(min = 8, max = 72) String confirmNewPassword
) {
}
