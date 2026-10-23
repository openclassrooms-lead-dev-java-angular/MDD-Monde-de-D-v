package com.openclassrooms.mddapi.topic.service;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.factory.LocalDateTimeTestFactory;
import com.openclassrooms.mddapi.subscription.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.subscription.service.SubscriptionService;
import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.mapper.TopicMapper;
import com.openclassrooms.mddapi.topic.repository.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
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

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenNoTopicsExist() {
        when(topicRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(topicRepository).findAll(pageable);
        verifyNoInteractions(userDetailsServiceImpl, subscriptionRepository, topicMapper);
    }

    @Test
    void findAll_shouldMapTopicsWithTheirSubscriptionStatus() {
        Topic topic1 = createTopic(1L, "Java");
        Topic topic2 = createTopic(2L, "Angular");

        Page<Topic> topics = new PageImpl<>(List.of(topic1, topic2), pageable, 2);

        TopicResponseDto dto1 = createDto("Java", true);
        TopicResponseDto dto2 = createDto("Angular", false);

        when(topicRepository.findAll(pageable)).thenReturn(topics);
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(100L);
        when(subscriptionRepository.findSubscribedTopicIds(100L, List.of(1L, 2L)))
                .thenReturn(Set.of(1L));
        when(topicMapper.toDto(topic1, true)).thenReturn(dto1);
        when(topicMapper.toDto(topic2, false)).thenReturn(dto2);

        Page<TopicResponseDto> result = topicService.findAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(dto1, result.getContent().get(0));
        assertEquals(dto2, result.getContent().get(1));

        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(subscriptionRepository).findSubscribedTopicIds(100L, List.of(1L, 2L));
        verify(topicMapper).toDto(topic1, true);
        verify(topicMapper).toDto(topic2, false);
    }

    @Test
    void subscribe_shouldSubscribeWhenTopicExists() {
        String publicId = "topic-public-id";
        Topic topic = createTopic(1L, "Java");

        when(topicRepository.existsByPublicId(publicId)).thenReturn(true);
        when(topicRepository.getReferenceByPublicId(publicId)).thenReturn(topic);

        assertDoesNotThrow(() -> topicService.subscribe(publicId));

        verify(topicRepository).existsByPublicId(publicId);
        verify(topicRepository).getReferenceByPublicId(publicId);
        verify(subscriptionService).subscribe(topic);
    }

    @Test
    void subscribe_shouldThrowWhenTopicDoesNotExist() {
        String publicId = "unknown-topic";

        when(topicRepository.existsByPublicId(publicId)).thenReturn(false);

        assertThrows(
                TopicNotFoundException.class,
                () -> topicService.subscribe(publicId)
        );

        verify(topicRepository).existsByPublicId(publicId);
        verify(topicRepository, never()).getReferenceByPublicId(anyString());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void unsubscribe_shouldUnsubscribeWhenTopicExists() {
        String publicId = "topic-public-id";
        Topic topic = createTopic(1L, "Java");

        when(topicRepository.existsByPublicId(publicId)).thenReturn(true);
        when(topicRepository.getReferenceByPublicId(publicId)).thenReturn(topic);

        assertDoesNotThrow(() -> topicService.unsubscribe(publicId));

        verify(topicRepository).existsByPublicId(publicId);
        verify(topicRepository).getReferenceByPublicId(publicId);
        verify(subscriptionService).unsubscribe(topic);
    }

    @Test
    void unsubscribe_shouldThrowWhenTopicDoesNotExist() {
        String publicId = "unknown-topic";

        when(topicRepository.existsByPublicId(publicId)).thenReturn(false);

        assertThrows(
                TopicNotFoundException.class,
                () -> topicService.unsubscribe(publicId)
        );

        verify(topicRepository).existsByPublicId(publicId);
        verify(topicRepository, never()).getReferenceByPublicId(anyString());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void existsByPublicId_shouldReturnTrueWhenTopicExists() {
        String publicId = "topic-public-id";
        when(topicRepository.existsByPublicId(publicId)).thenReturn(true);

        boolean result = topicService.existsByPublicId(publicId);

        assertTrue(result);
        verify(topicRepository).existsByPublicId(publicId);
    }

    @Test
    void existsByPublicId_shouldReturnFalseWhenTopicDoesNotExist() {
        String publicId = "unknown-topic";
        when(topicRepository.existsByPublicId(publicId)).thenReturn(false);

        boolean result = topicService.existsByPublicId(publicId);

        assertFalse(result);
        verify(topicRepository).existsByPublicId(publicId);
    }

    @Test
    void loadByPublicId_shouldReturnTopicReference() {
        String publicId = "topic-public-id";
        Topic topic = createTopic(1L, "Java");

        when(topicRepository.getReferenceByPublicId(publicId)).thenReturn(topic);

        Topic result = topicService.loadByPublicId(publicId);

        assertSame(topic, result);
        verify(topicRepository).getReferenceByPublicId(publicId);
    }

    @Test
    void loadByPublicId_shouldPropagateEntityNotFoundException() {
        String publicId = "unknown-topic";

        when(topicRepository.getReferenceByPublicId(publicId))
                .thenThrow(new EntityNotFoundException());

        assertThrows(
                EntityNotFoundException.class,
                () -> topicService.loadByPublicId(publicId)
        );
    }

    @Test
    void getOptions_shouldReturnTopicOptions() {
        List<TopicOptionDto> options = List.of(
                mock(TopicOptionDto.class),
                mock(TopicOptionDto.class)
        );

        when(topicRepository.findAllTopicOptions()).thenReturn(options);

        List<TopicOptionDto> result = topicService.getOptions();

        assertSame(options, result);
        verify(topicRepository).findAllTopicOptions();
    }

    @Test
    void getOptions_shouldReturnEmptyListWhenNoOptionsExist() {
        when(topicRepository.findAllTopicOptions()).thenReturn(List.of());

        List<TopicOptionDto> result = topicService.getOptions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSubscribedTopics_shouldReturnMappedSubscribedTopics() {
        Long userId = 100L;
        Topic topic1 = createTopic(1L, "Java");
        Topic topic2 = createTopic(2L, "Angular");

        Page<Topic> topics = new PageImpl<>(List.of(topic1, topic2), pageable, 2);

        TopicResponseDto dto1 = createDto("Java", true);
        TopicResponseDto dto2 = createDto("Angular", true);

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(topicRepository.findSubscribedTopics(pageable, userId)).thenReturn(topics);
        when(topicMapper.toDto(topic1, true)).thenReturn(dto1);
        when(topicMapper.toDto(topic2, true)).thenReturn(dto2);

        Page<TopicResponseDto> result = topicService.getSubscribedTopics(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(dto1, dto2), result.getContent());

        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(topicRepository).findSubscribedTopics(pageable, userId);
        verify(topicMapper).toDto(topic1, true);
        verify(topicMapper).toDto(topic2, true);
    }

    @Test
    void getSubscribedTopics_shouldReturnEmptyPageWhenNoTopicsAreSubscribed() {
        Long userId = 100L;

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(topicRepository.findSubscribedTopics(pageable, userId))
                .thenReturn(Page.empty(pageable));

        Page<TopicResponseDto> result = topicService.getSubscribedTopics(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(topicRepository).findSubscribedTopics(pageable, userId);
        verifyNoInteractions(topicMapper);
    }

    private Topic createTopic(Long id, String name) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setPublicId("public-id-" + id);
        topic.setName(name);
        topic.setDescription(name + " description");
        return topic;
    }

    private TopicResponseDto createDto(String name, Boolean subscribed) {
        return new TopicResponseDto(
                "public-id-" + name,
                name,
                name + " description",
                subscribed,
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt()
        );
    }
}