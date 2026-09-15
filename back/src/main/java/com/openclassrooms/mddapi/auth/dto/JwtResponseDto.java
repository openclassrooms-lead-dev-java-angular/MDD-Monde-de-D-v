package com.openclassrooms.mddapi.auth.dto;

import org.springframework.http.HttpStatus;

public record JwtResponseDto(
        HttpStatus status,
        String error,
        String message
) { }
