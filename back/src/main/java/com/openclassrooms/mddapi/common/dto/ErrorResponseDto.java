package com.openclassrooms.mddapi.common.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponseDto(
        HttpStatus status,
        String message,
        String timestamp
) { }

