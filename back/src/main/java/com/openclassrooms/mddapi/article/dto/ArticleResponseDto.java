package com.openclassrooms.mddapi.article.dto;

import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;

import java.time.LocalDateTime;

public record ArticleResponseDto(
        String username,
        TopicResponseDto topic,
        String slug,
        String title,
        String content,
        String media,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
