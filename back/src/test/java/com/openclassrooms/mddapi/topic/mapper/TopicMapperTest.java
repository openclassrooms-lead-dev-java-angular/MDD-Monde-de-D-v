package com.openclassrooms.mddapi.topic.mapper;

import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
class TopicMapperTest {

    private final TopicMapper topicMapper =
            Mappers.getMapper(TopicMapper.class);

    @Test
    void shouldMapTopicToTopicResponseDtoWhenSubscribed() {
        Topic topic = TopicTestFactory.createTopic();

        TopicResponseDto result = topicMapper.toDto(topic, true);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(topic.getName());
        assertThat(result.slug()).isEqualTo(topic.getSlug());
        assertThat(result.description()).isEqualTo(topic.getDescription());
        assertThat(result.subscribed()).isTrue();
        assertThat(result.createdAt()).isEqualTo(topic.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(topic.getUpdatedAt());
    }

    @Test
    void shouldMapTopicToTopicResponseDtoWhenNotSubscribed() {
        Topic topic = TopicTestFactory.createTopic();

        TopicResponseDto result = topicMapper.toDto(topic, false);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(topic.getName());
        assertThat(result.slug()).isEqualTo(topic.getSlug());
        assertThat(result.description()).isEqualTo(topic.getDescription());
        assertThat(result.subscribed()).isFalse();
        assertThat(result.createdAt()).isEqualTo(topic.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(topic.getUpdatedAt());
    }

    @Test
    void shouldMapTopicToTopicResponseDtoWhenSubscriptionIsNull() {
        Topic topic = TopicTestFactory.createTopic();

        TopicResponseDto result = topicMapper.toDto(topic, null);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(topic.getName());
        assertThat(result.slug()).isEqualTo(topic.getSlug());
        assertThat(result.description()).isEqualTo(topic.getDescription());
        assertThat(result.subscribed()).isNull();
        assertThat(result.createdAt()).isEqualTo(topic.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(topic.getUpdatedAt());
    }
}