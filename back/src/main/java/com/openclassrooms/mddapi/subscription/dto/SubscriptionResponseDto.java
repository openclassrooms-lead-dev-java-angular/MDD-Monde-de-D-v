package com.openclassrooms.mddapi.subscription.dto;

import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;

import java.time.LocalDateTime;

public record SubscriptionResponseDto(
        TopicResponseDto topic,
        LocalDateTime createdAt
) { }
