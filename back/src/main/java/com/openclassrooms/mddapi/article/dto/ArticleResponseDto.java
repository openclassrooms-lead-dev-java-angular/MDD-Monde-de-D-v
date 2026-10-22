package com.openclassrooms.mddapi.article.dto;

import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleResponseDto(
        String username,
        TopicResponseDto topic,
        String slug,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponseDto> comments
) { }
