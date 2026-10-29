package com.openclassrooms.mddapi.topic.mapper;

import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class TopicMapperTest {

    private final TopicMapper topicMapper =
            Mappers.getMapper(TopicMapper.class);

    @Test
    void toDto_shouldMapTopicWithSubscribedTrue() {
        Topic topic = new Topic();
        topic.setPublicId("550e8400-e29b-41d4-a716-446655440000");
        topic.setName("Java");
        topic.setDescription("Java programming");

        TopicResponseDto result = topicMapper.toDto(topic, true);

        assertNotNull(result);
        assertEquals(
                "550e8400-e29b-41d4-a716-446655440000",
                result.publicId()
        );
        assertEquals("Java", result.name());
        assertEquals("Java programming", result.description());
        assertTrue(result.subscribed());
    }

    @Test
    void toDto_shouldMapTopicWithSubscribedFalse() {
        Topic topic = new Topic();
        topic.setPublicId("550e8400-e29b-41d4-a716-446655440000");
        topic.setName("Java");
        topic.setDescription("Java programming");

        TopicResponseDto result = topicMapper.toDto(topic, false);

        assertNotNull(result);
        assertFalse(result.subscribed());
    }

    @Test
    void toDto_shouldMapTopicWithSubscribedNull() {
        Topic topic = new Topic();
        topic.setPublicId("550e8400-e29b-41d4-a716-446655440000");
        topic.setName("Java");
        topic.setDescription("Java programming");

        TopicResponseDto result = topicMapper.toDto(topic, null);

        assertNotNull(result);
        assertNull(result.subscribed());
    }

    @Test
    void toDto_shouldMapSubscribedWhenTopicIsNull() {
        TopicResponseDto result = topicMapper.toDto(null, true);

        assertNotNull(result);
        assertNull(result.publicId());
        assertNull(result.name());
        assertNull(result.description());
        assertTrue(result.subscribed());
    }
}