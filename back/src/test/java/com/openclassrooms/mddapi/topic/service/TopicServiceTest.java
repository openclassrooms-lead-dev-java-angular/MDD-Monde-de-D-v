package com.openclassrooms.mddapi.topic.service;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
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

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

    private Topic topic1;
    private Topic topic2;

    private TopicResponseDto response1;
    private TopicResponseDto response2;

    @BeforeEach
    void setUp() {
        topic1 = mock(Topic.class);
        topic2 = mock(Topic.class);

        when(topic1.getId()).thenReturn(1L);
        when(topic2.getId()).thenReturn(2L);

        response1 = mock(TopicResponseDto.class);
        response2 = mock(TopicResponseDto.class);
    }

    // --------------------------------------------------
    // findAll
    // --------------------------------------------------

    @Test
    void shouldReturnEmptyPageWhenNoTopicsExist() {
        Pageable pageable = PageRequest.of(0, 10);

        when(topicRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(topicRepository).findAll(pageable);
        verifyNoInteractions(
                topicMapper,
                subscriptionRepository,
                userDetailsServiceImpl
        );
    }

    @Test
    void shouldReturnTopicsWithSubscriptionStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Topic> topics = new PageImpl<>(
                List.of(topic1, topic2),
                pageable,
                2
        );

        when(topicRepository.findAll(pageable)).thenReturn(topics);
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(42L);

        when(subscriptionRepository.findSubscribedTopicIds(
                42L,
                List.of(1L, 2L)
        )).thenReturn(Set.of(1L));

        when(topicMapper.toDto(topic1, true)).thenReturn(response1);
        when(topicMapper.toDto(topic2, false)).thenReturn(response2);

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(response1, response2), result.getContent());

        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(subscriptionRepository)
                .findSubscribedTopicIds(42L, List.of(1L, 2L));

        verify(topicMapper).toDto(topic1, true);
        verify(topicMapper).toDto(topic2, false);
    }

    @Test
    void shouldReturnEmptyPageWithoutLoadingCurrentUser() {
        Pageable pageable = PageRequest.of(0, 10);

        when(topicRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertTrue(result.isEmpty());

        verify(topicRepository).findAll(pageable);
        verifyNoInteractions(
                topicMapper,
                subscriptionRepository,
                userDetailsServiceImpl
        );
    }

    @Test
    void shouldPreservePaginationInformation() {
        Pageable pageable = PageRequest.of(1, 10);

        Page<Topic> topics = new PageImpl<>(
                List.of(topic1),
                pageable,
                21
        );

        when(topicRepository.findAll(pageable)).thenReturn(topics);
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(42L);
        when(subscriptionRepository.findSubscribedTopicIds(
                42L,
                List.of(1L)
        )).thenReturn(Set.of());

        when(topicMapper.toDto(topic1, false)).thenReturn(response1);

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertEquals(1, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(21, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
    }

    // --------------------------------------------------
    // subscribe
    // --------------------------------------------------

    @Test
    void shouldSubscribeToExistingTopic() {
        String slug = "java";

        when(topicRepository.existsBySlug(slug)).thenReturn(true);
        when(topicRepository.getReferenceBySlug(slug)).thenReturn(topic1);

        assertDoesNotThrow(() -> topicService.subscribe(slug));

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository).getReferenceBySlug(slug);
        verify(subscriptionService).subscribe(topic1);
    }

    @Test
    void shouldThrowExceptionWhenSubscribingToUnknownTopic() {
        String slug = "unknown-topic";

        when(topicRepository.existsBySlug(slug)).thenReturn(false);

        TopicNotFoundException exception = assertThrows(
                TopicNotFoundException.class,
                () -> topicService.subscribe(slug)
        );

        assertEquals(
                "Topic not found with slug '" + slug + "'",
                exception.getMessage()
        );

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository, never()).getReferenceBySlug(anyString());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void shouldNotSubscribeWhenTopicDoesNotExist() {
        String slug = "unknown-topic";

        when(topicRepository.existsBySlug(slug)).thenReturn(false);

        assertThrows(
                TopicNotFoundException.class,
                () -> topicService.subscribe(slug)
        );

        verify(subscriptionService, never()).subscribe(any(Topic.class));
    }

    // --------------------------------------------------
    // unsubscribe
    // --------------------------------------------------

    @Test
    void shouldUnsubscribeFromExistingTopic() {
        String slug = "java";

        when(topicRepository.existsBySlug(slug)).thenReturn(true);
        when(topicRepository.getReferenceBySlug(slug)).thenReturn(topic1);

        assertDoesNotThrow(() -> topicService.unsubscribe(slug));

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository).getReferenceBySlug(slug);
        verify(subscriptionService).unsubscribe(topic1);
    }

    @Test
    void shouldThrowExceptionWhenUnsubscribingFromUnknownTopic() {
        String slug = "unknown-topic";

        when(topicRepository.existsBySlug(slug)).thenReturn(false);

        TopicNotFoundException exception = assertThrows(
                TopicNotFoundException.class,
                () -> topicService.unsubscribe(slug)
        );

        assertEquals(
                "Topic not found with slug '" + slug + "'",
                exception.getMessage()
        );

        verify(topicRepository).existsBySlug(slug);
        verify(topicRepository, never()).getReferenceBySlug(anyString());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void shouldNotUnsubscribeWhenTopicDoesNotExist() {
        String slug = "unknown-topic";

        when(topicRepository.existsBySlug(slug)).thenReturn(false);

        assertThrows(
                TopicNotFoundException.class,
                () -> topicService.unsubscribe(slug)
        );

        verify(subscriptionService, never()).unsubscribe(any(Topic.class));
    }

    // --------------------------------------------------
    // existsBySlug
    // --------------------------------------------------

    @Test
    void shouldReturnTrueWhenTopicExists() {
        when(topicRepository.existsBySlug("java")).thenReturn(true);

        assertTrue(topicService.existsBySlug("java"));

        verify(topicRepository).existsBySlug("java");
    }

    @Test
    void shouldReturnFalseWhenTopicDoesNotExist() {
        when(topicRepository.existsBySlug("unknown-topic")).thenReturn(false);

        assertFalse(topicService.existsBySlug("unknown-topic"));

        verify(topicRepository).existsBySlug("unknown-topic");
    }

    // --------------------------------------------------
    // loadBySlug
    // --------------------------------------------------

    @Test
    void shouldLoadTopicBySlug() {
        String slug = "java";

        when(topicRepository.getReferenceBySlug(slug)).thenReturn(topic1);

        Topic result = topicService.loadBySlug(slug);

        assertSame(topic1, result);
        verify(topicRepository).getReferenceBySlug(slug);
    }

    @Test
    void shouldPropagateExceptionWhenLoadingTopicFails() {
        String slug = "unknown-topic";
        RuntimeException exception = new RuntimeException("Topic not found");

        when(topicRepository.getReferenceBySlug(slug))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> topicService.loadBySlug(slug)
        );

        assertSame(exception, thrown);
        verify(topicRepository).getReferenceBySlug(slug);
    }

    // --------------------------------------------------
    // getOptions
    // --------------------------------------------------

    @Test
    void shouldReturnTopicOptions() {
        List<TopicOptionDto> expected = List.of(
                mock(TopicOptionDto.class),
                mock(TopicOptionDto.class)
        );

        when(topicRepository.findAllTopicOptions()).thenReturn(expected);

        List<TopicOptionDto> result = topicService.getOptions();

        assertEquals(expected, result);
        assertEquals(2, result.size());

        verify(topicRepository).findAllTopicOptions();
    }

    @Test
    void shouldReturnEmptyListWhenNoTopicOptionsExist() {
        when(topicRepository.findAllTopicOptions()).thenReturn(List.of());

        List<TopicOptionDto> result = topicService.getOptions();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(topicRepository).findAllTopicOptions();
    }
}