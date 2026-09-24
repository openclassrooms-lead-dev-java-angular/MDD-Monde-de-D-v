package com.openclassrooms.mddapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;


public record UserResponseDto(
        @NotNull
        Long id,

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
        String lastName,

        String avatar,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) { }
