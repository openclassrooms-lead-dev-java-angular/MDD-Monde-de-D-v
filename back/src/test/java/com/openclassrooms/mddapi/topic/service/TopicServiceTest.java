package com.openclassrooms.mddapi.topic.service;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.subscription.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.subscription.service.SubscriptionService;
import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.mapper.TopicMapper;
import com.openclassrooms.mddapi.topic.repository.TopicRepository;
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
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private TopicService topicService;

    @Test
    void shouldReturnEmptyPageWhenNoTopicsExist() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Topic> emptyPage = Page.empty(pageable);

        when(topicRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(topicRepository).findAll(pageable);
        verifyNoInteractions(
                userDetailsServiceImpl,
                subscriptionRepository,
                topicMapper
        );
    }

    @Test
    void shouldReturnTopicsWithSubscriptionStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Topic topic1 = TopicTestFactory.createTopic();
        topic1.setId(1L);

        Topic topic2 = TopicTestFactory.createTopic();
        topic2.setId(2L);

        List<Topic> topics = List.of(topic1, topic2);
        Page<Topic> topicPage = new PageImpl<>(topics, pageable, topics.size());

        TopicResponseDto response1 = TopicTestFactory.createTopicResponseDto();
        TopicResponseDto response2 = TopicTestFactory.createTopicResponseDto();

        when(topicRepository.findAll(pageable)).thenReturn(topicPage);
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(10L);
        when(subscriptionRepository.findSubscribedTopicIds(10L, List.of(1L, 2L)))
                .thenReturn(Set.of(1L));
        when(topicMapper.toDto(topic1, true)).thenReturn(response1);
        when(topicMapper.toDto(topic2, false)).thenReturn(response2);

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(response1, result.getContent().get(0));
        assertEquals(response2, result.getContent().get(1));
        assertEquals(2, result.getTotalElements());

        verify(topicRepository).findAll(pageable);
        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(subscriptionRepository).findSubscribedTopicIds(10L, List.of(1L, 2L));
        verify(topicMapper).toDto(topic1, true);
        verify(topicMapper).toDto(topic2, false);
    }

    @Test
    void shouldSubscribeCurrentUserToTopic() {
        String slug = "java";
        Topic topic = TopicTestFactory.createTopic();

        when(topicRepository.existsBySlug(slug)).thenReturn(true);
        when(topicRepository.getReferenceBySlug(slug)).thenReturn(topic);

        topicService.subscribe(slug);

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository).getReferenceBySlug(slug);
        verify(subscriptionService).subscribe(topic);
    }

    @Test
    void shouldThrowTopicNotFoundExceptionWhenSubscribingToUnknownTopic() {
        String slug = "unknown-topic";

        when(topicRepository.existsBySlug(slug)).thenReturn(false);

        assertThatThrownBy(() -> topicService.subscribe(slug))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found with slug '" + slug + "'");

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository, never()).getReferenceBySlug(anyString());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void shouldUnsubscribeCurrentUserFromTopic() {
        String slug = "java";
        Topic topic = TopicTestFactory.createTopic();

        when(topicRepository.existsBySlug(slug)).thenReturn(true);
        when(topicRepository.getReferenceBySlug(slug)).thenReturn(topic);

        topicService.unsubscribe(slug);

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository).getReferenceBySlug(slug);
        verify(subscriptionService).unsubscribe(topic);
    }

    @Test
    void shouldThrowTopicNotFoundExceptionWhenUnsubscribingFromUnknownTopic() {
        String slug = "unknown-topic";

        when(topicRepository.existsBySlug(slug)).thenReturn(false);

        assertThatThrownBy(() -> topicService.unsubscribe(slug))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found with slug '" + slug + "'");

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository, never()).getReferenceBySlug(anyString());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void shouldReturnTrueWhenTopicSlugExists() {
        String slug = "java";
        when(topicRepository.existsBySlug(slug)).thenReturn(true);

        boolean result = topicService.existsBySlug(slug);

        assertThat(result).isTrue();
        verify(topicRepository).existsBySlug(slug);
    }

    @Test
    void shouldReturnFalseWhenTopicSlugDoesNotExist() {
        String slug = "unknown-topic";
        when(topicRepository.existsBySlug(slug)).thenReturn(false);

        boolean result = topicService.existsBySlug(slug);

        assertThat(result).isFalse();
        verify(topicRepository).existsBySlug(slug);
    }

    @Test
    void shouldLoadTopicBySlug() {
        String slug = "java";
        Topic topic = TopicTestFactory.createTopic();

        when(topicRepository.getReferenceBySlug(slug)).thenReturn(topic);

        Topic result = topicService.loadBySlug(slug);

        assertThat(result).isSameAs(topic);
        verify(topicRepository).getReferenceBySlug(slug);
    }

    @Test
    void shouldReturnTopicOptions() {
        List<TopicOptionDto> options = List.of(
                new TopicOptionDto("Java", "java"),
                new TopicOptionDto("Spring", "spring")
        );

        when(topicRepository.findAllTopicOptions()).thenReturn(options);

        List<TopicOptionDto> result = topicService.getOptions();

        assertEquals(2, result.size());
        assertEquals(options, result);

        verify(topicRepository).findAllTopicOptions();
    }

    @Test
    void shouldReturnEmptyListWhenNoTopicOptionsExist() {
        when(topicRepository.findAllTopicOptions()).thenReturn(List.of());

        List<TopicOptionDto> result = topicService.getOptions();

        assertTrue(result.isEmpty());
        verify(topicRepository).findAllTopicOptions();
    }
}