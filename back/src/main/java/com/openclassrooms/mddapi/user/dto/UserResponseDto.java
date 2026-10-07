package com.openclassrooms.mddapi.user.dto;

import java.time.LocalDateTime;


public record UserResponseDto(
        String email,

        String username,

        String avatar,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) { }
