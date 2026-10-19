package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public class TopicTestFactory {

    public static TopicResponseDto createTopicResponseDto() {
        return new TopicResponseDto(
                "Java",
                "java",
                "Java development.",
                false,
                generateCreatedAt(),
                generateUpdatedAt()
        );
    }

    public static List<TopicResponseDto> createTopicResponseDtoList() {
        TopicResponseDto topic1 = new TopicResponseDto(
                "Java",
                "java",
                "Java development.",
                false,
                generateCreatedAt(),
                generateUpdatedAt()
        );

        TopicResponseDto topic2 = new TopicResponseDto(
                "Python",
                "python",
                "Python development.",
                false,
                generateCreatedAt(),
                generateUpdatedAt()
        );

        return List.of(topic1, topic2);
    }

    public static Topic createTopic() {

        return generateTopic(
                1L,
                "Java",
                "java",
                "Java development"
        );
    }

    private static Topic generateTopic(Long id, String name, String slug, String description) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setSlug(slug);
        topic.setName(name);
        topic.setDescription(description);
        topic.setCreatedAt(generateCreatedAt());
        topic.setUpdatedAt(generateUpdatedAt());

        return topic;
    }

    private static LocalDateTime generateCreatedAt() {
        return LocalDateTime.of(2026, 9, 16, 10, 0);
    }

    private static LocalDateTime generateUpdatedAt() {
        return LocalDateTime.of(2026, 9, 16, 11, 0);
    }
}
