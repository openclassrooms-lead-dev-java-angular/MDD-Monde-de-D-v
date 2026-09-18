package com.openclassrooms.mddapi.topic.mapper;

import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.topic.dto.TopicRequestDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TopicMapperTest {
    private final TopicMapper topicMapper = Mappers.getMapper(TopicMapper.class);

    @Test
    void shouldMapTopicToTopicResponseDto() {
        Topic topic = TopicTestFactory.createTopic();
        TopicResponseDto result = topicMapper.toDto(topic);

        assertThat(result)
                .isNotNull();
        assertThat(result.id())
                .isEqualTo(topic.getId());
        assertThat(result.name())
                .isEqualTo(topic.getName());
        assertThat(result.slug())
                .isEqualTo(topic.getSlug());
        assertThat(result.description())
                .isEqualTo(topic.getDescription());
        assertThat(result.createdAt())
                .isEqualTo(topic.getCreatedAt());
        assertThat(result.updatedAt())
                .isEqualTo(topic.getUpdatedAt());
    }

    @Test
    void shouldMapTopicRequestDtoToTopic() {
        TopicRequestDto requestDto = TopicTestFactory.createTopicRequestDto();

        Topic result = topicMapper.toEntity(requestDto);

        assertThat(result)
                .isNotNull();
        assertThat(result.getName())
                .isEqualTo(requestDto.name());
        assertThat(result.getSlug())
                .isEqualTo(requestDto.slug());
        assertThat(result.getDescription())
                .isEqualTo(requestDto.description());
    }

    @Test
    void shouldUpdateTopicFromTopicRequestDto() {
        Topic topic = TopicTestFactory.createTopic();
        TopicRequestDto requestDto = TopicTestFactory.createTopicRequestDto();

        Long id = topic.getId();
        topicMapper.updateEntity(topic, requestDto);

        assertThat(topic.getId())
                .isEqualTo(id);
        assertThat(topic.getName())
                .isEqualTo(requestDto.name());
        assertThat(topic.getSlug())
                .isEqualTo(requestDto.slug());
        assertThat(topic.getDescription())
                .isEqualTo(requestDto.description());
    }
}
