package com.openclassrooms.mddapi.topic;

import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.topic.dto.TopicRequestDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.exception.TopicSlugAlreadyExists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TopicService topicService;

    // Find topic by slug

    @Test
    void shouldReturnTopicResponseDtoWhenSlugExists() {
        String slug = "Java";
        Topic topic = TopicTestFactory.createTopic();
        TopicResponseDto response = TopicTestFactory.createTopicResponseDto();

        when(topicRepository.findBySlug(slug))
                .thenReturn(Optional.of(topic));

        when(topicMapper.toDto(topic))
                .thenReturn(response);

        TopicResponseDto result = topicService.findBySlug(slug);

        assertThat(result).isSameAs(response);

        verify(topicRepository).findBySlug(slug);
        verify(topicMapper).toDto(topic);
    }

    @Test
    void shouldThrowTopicNotFoundExceptionWhenSlugDoesNotExist() {
        String slug = "Java";

        when(topicRepository.findBySlug(slug))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.findBySlug(slug))
                .isInstanceOf(TopicNotFoundException.class);

        verify(topicRepository).findBySlug(slug);
        verifyNoInteractions(topicMapper);
    }

    // Find all topics

    @Test
    void shouldReturnAllTopics() {
        Pageable pageable = PageRequest.of(0, 10);

        List<Topic> topics = TopicTestFactory.createTopicList();

        Page<Topic> topicPage = new PageImpl<>(topics, pageable, topics.size());

        List<TopicResponseDto> topicResponseDtos = TopicTestFactory.createTopicResponseDtoList();

        when(topicRepository.findAll(pageable))
                .thenReturn(topicPage);

        for (int i = 0; i < topics.size(); i++) {
            when(topicMapper.toDto(topics.get(i)))
                    .thenReturn(topicResponseDtos.get(i));
        }

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertThat(result)
                .isNotNull();
        assertThat(result.getContent().get(0))
                .isEqualTo(topicResponseDtos.get(0));
        assertThat(result.getContent().get(1))
                .isEqualTo(topicResponseDtos.get(1));
        assertThat(result.getTotalElements())
                .isEqualTo(topicResponseDtos.size());
        verify(topicRepository).findAll(pageable);

        for (Topic topic : topics) {
            verify(topicMapper).toDto(topic);
        }
    }

    // Create topic

    @Test
    void shouldCreateTopic() {
        TopicRequestDto requestDto = TopicTestFactory.createTopicRequestDto();
        Topic topic = TopicTestFactory.createTopic();
        Topic savedTopic = TopicTestFactory.createTopic();
        savedTopic.setId(1L);
        TopicResponseDto topicResponseDto = TopicTestFactory.createTopicResponseDto();

        when(topicMapper.toEntity(requestDto))
                .thenReturn(topic);

        when(topicRepository.save(topic))
                .thenReturn(savedTopic);

        when(topicMapper.toDto(topic))
                .thenReturn(topicResponseDto);

        TopicResponseDto result = topicService.create(requestDto);

        assertThat(result).isSameAs(topicResponseDto);

        verify(topicRepository).save(topic);
        verify(topicMapper).toEntity(requestDto);
    }

    @Test
    void shouldThrowTopicSlugAlreadyExistsWhenSlugAlreadyExists() {
        TopicRequestDto requestDto = TopicTestFactory.createTopicRequestDto();
        Topic topic = TopicTestFactory.createTopic();

        when(topicMapper.toEntity(requestDto))
                .thenReturn(topic);
        when(topicRepository.save(topic))
                .thenThrow(new TopicSlugAlreadyExists("Topic already exists with slug : " + requestDto.slug()));

        assertThatThrownBy(() -> topicService.create(requestDto))
                .isInstanceOf(TopicSlugAlreadyExists.class);

        verify(topicRepository).save(topic);
        verify(topicMapper).toEntity(requestDto);
        verifyNoMoreInteractions(topicMapper);
    }

    // Update topic

    @Test
    void shouldUpdateTopic() {
        String slug = "java";

        TopicRequestDto requestDto = TopicTestFactory.createTopicRequestDto();

        Topic topic = TopicTestFactory.createTopic();

        TopicResponseDto response = TopicTestFactory.createTopicResponseDto();

        when(topicRepository.existsBySlug(slug))
                .thenReturn(true);
        when(topicRepository.getReferenceBySlug(slug))
                .thenReturn(topic);
        when(topicRepository.save(topic))
                .thenReturn(topic);
        when(topicMapper.toDto(topic))
                .thenReturn(response);

        TopicResponseDto result = topicService.update(slug, requestDto);

        assertThat(result).isEqualTo(response);

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository).getReferenceBySlug(slug);
        verify(topicMapper).updateEntity(topic, requestDto);
        verify(topicRepository).save(topic);
        verify(topicMapper).toDto(topic);
    }

    @Test
    void shouldThrowTopicNotFoundExceptionWhenUpdatingNonExistingTopic() {
        String slug = "unknown-slug";

        TopicRequestDto requestDto = TopicTestFactory.createTopicRequestDto();

        when(topicRepository.existsBySlug(slug))
                .thenReturn(false);

        assertThatThrownBy(() -> topicService.update(slug, requestDto)).isInstanceOf(TopicNotFoundException.class).hasMessage("Topic not found");

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository, never()).getReferenceBySlug(slug);
        verify(topicRepository, never()).save(any());

        verifyNoInteractions(topicMapper);
    }

    // Available Slug

    @Test
    void shouldReturnSlugAvailableWhenSlugDoesNotExist() {
        String slug = "Java";

        when(topicRepository.existsBySlug(slug))
                .thenReturn(false);

        AvailableSlugDto result = topicService.availableSlug(slug);

        assertThat(result.available())
                .isTrue();

        verify(topicRepository).existsBySlug(slug);
    }

    @Test
    void shouldReturnSlugUnavailableWhenSlugAlreadyExists() {
        String slug = "Java";

        when(topicRepository.existsBySlug(slug))
                .thenReturn(true);

        AvailableSlugDto result = topicService.availableSlug(slug);

        assertThat(result.available())
                .isFalse();

        verify(topicRepository).existsBySlug(slug);
    }

    // Subscription

    @Test
    void shouldSubscribeCurrentUserToTopic() {
        // todo
    }

    @Test
    void shouldUnsubscribeCurrentUserFromTopic() {
        // todo
    }
}
