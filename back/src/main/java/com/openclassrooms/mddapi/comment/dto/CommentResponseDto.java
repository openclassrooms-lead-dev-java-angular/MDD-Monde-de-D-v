package com.openclassrooms.mddapi.comment.dto;

import java.time.LocalDateTime;

public record CommentResponseDto(
        String username,
        String content,
        LocalDateTime createdAt
) { }
