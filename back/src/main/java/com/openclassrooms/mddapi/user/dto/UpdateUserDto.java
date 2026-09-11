package com.openclassrooms.mddapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(
        @NotBlank
        @Size(max = 50)
        @Email
        String email,

        @NotBlank
        @Size(max = 20)
        String username,

        @NotBlank
        @Size(max = 50)
        String firstName,

        @NotBlank
        @Size(max = 50)
        String lastName
) { }
