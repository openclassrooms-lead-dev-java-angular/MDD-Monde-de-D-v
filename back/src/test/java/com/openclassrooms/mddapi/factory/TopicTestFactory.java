package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.topic.entity.Topic;
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
                generateCreatedAt(),
                generateUpdatedAt()
        );
    }

    public static List<TopicResponseDto> createTopicResponseDtoList() {
        TopicResponseDto topic1 = new TopicResponseDto(
                1L,
                "Java",
                "java",
                "Java development.",
                generateCreatedAt(),
                generateUpdatedAt()
        );

        TopicResponseDto topic2 = new TopicResponseDto(
                2L,
                "Python",
                "python",
                "Python development.",
                generateCreatedAt(),
                generateUpdatedAt()
        );

        return List.of(topic1, topic2);
    }

    public static Page<TopicResponseDto> createPageableTopicsResponseDto() {

        TopicResponseDto topic1 = new TopicResponseDto(
                1L,
                "Java",
                "java",
                "Java development.",
                generateCreatedAt(),
                generateUpdatedAt()
        );

        TopicResponseDto topic2 = new TopicResponseDto(
                2L,
                "Python",
                "python",
                "Python development.",
                generateCreatedAt(),
                generateUpdatedAt()
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

    public static Topic createTopic() {
        return generateTopic(1L, "Java", "java", "Java development");
    }

    public static List<Topic> createTopicList() {
        return List.of(
                generateTopic(1L, "Java", "java", "Java development"),
                generateTopic(2L, "Python", "python", "Python development")
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
