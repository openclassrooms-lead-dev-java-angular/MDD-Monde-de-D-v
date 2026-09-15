package com.openclassrooms.mddapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "error.register.email.required")
        @Size(max = 50, message = "error.register.email.length")
        @Email(message = "error.register.email.invalid")
        String email,

        @NotBlank(message = "error.register.password.required")
        @Size(max = 50, min = 8, message = "error.register.password.length")
        @Pattern(regexp = ".*\\d.*", message = "error.register.password.missing_number")
        @Pattern(regexp = ".*[a-z].*", message = "error.register.password.missing_lowercase")
        @Pattern(regexp = ".*[A-Z].*", message = "error.register.password.missing_uppercase")
        @Pattern(regexp = ".*[^a-zA-Z0-9].*", message = "error.register.password.missing_special_char")
        String password,

        @NotBlank(message = "error.register.username.required")
        @Size(max = 20, message = "error.register.username.length")
        String username,

        @NotBlank(message = "error.register.firstname.required")
        @Size(max = 50, message = "error.register.firstname.length")
        String firstName,

        @NotBlank(message = "error.register.lastname.required")
        @Size(max = 50, message = "error.register.lastname.length")
        String lastName
) { }
