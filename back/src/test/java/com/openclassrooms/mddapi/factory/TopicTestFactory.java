package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.topic.dto.TopicRequestDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

public class TopicTestFactory {

    public static TopicResponseDto createTopicResponseDto() {
        return new TopicResponseDto(
                1L,
                "Java",
                "java",
                "Java development.",
                LocalDateTime.of(2026, 9, 16, 10, 0),
                LocalDateTime.of(2026, 9, 16, 11, 0)

        );
    }

    public static Page<TopicResponseDto> createTopicsRequestDto() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 16, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 16, 11, 0);

        TopicResponseDto topic1 = new TopicResponseDto(
                1L,
                "Java",
                "java",
                "Java development.",
                createdAt,
                updatedAt
        );

        TopicResponseDto topic2 = new TopicResponseDto(
                2L,
                "Python",
                "python",
                "Python development.",
                createdAt,
                updatedAt
        );

        return new PageImpl<>(
                List.of(topic1, topic2),
                PageRequest.of(0, 10),
                2
        );
    }

    public static TopicRequestDto createTopicRequestDto() {
        return new TopicRequestDto(
                "Java",
                "java",
                "Welcome to java language topic"
        );
    }
}
