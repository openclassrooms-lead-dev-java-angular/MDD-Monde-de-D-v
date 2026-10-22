package com.openclassrooms.mddapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(
        @NotBlank
        @Size(max = 50)
        @Email
        String email,

        @NotBlank
        @Size(max = 20)
        String username,

        @NotBlank(message = "error.register.password.required")
        @Size(max = 50, min = 8, message = "error.register.password.length")
        @Pattern(regexp = ".*\\d.*", message = "error.register.password.missing_number")
        @Pattern(regexp = ".*[a-z].*", message = "error.register.password.missing_lowercase")
        @Pattern(regexp = ".*[A-Z].*", message = "error.register.password.missing_uppercase")
        @Pattern(regexp = ".*[^a-zA-Z0-9].*", message = "error.register.password.missing_special_char")
        String password
) { }
