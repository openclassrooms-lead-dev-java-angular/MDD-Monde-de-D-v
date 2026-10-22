package com.openclassrooms.mddapi.user.dto;

import java.time.LocalDateTime;


public record UserResponseDto(
        String email,

        String username,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) { }
