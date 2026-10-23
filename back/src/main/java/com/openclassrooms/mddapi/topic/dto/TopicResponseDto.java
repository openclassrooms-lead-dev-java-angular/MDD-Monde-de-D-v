package com.openclassrooms.mddapi.topic.dto;

import java.time.LocalDateTime;

public record TopicResponseDto(
        String publicId,
        String name,
        String description,
        Boolean subscribed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
