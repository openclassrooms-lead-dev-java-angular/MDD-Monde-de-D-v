package com.openclassrooms.mddapi.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "error.register.email.required")
        String usernameOrEmail,

        @NotBlank(message = "error.register.password.required")
        String password
) { }
