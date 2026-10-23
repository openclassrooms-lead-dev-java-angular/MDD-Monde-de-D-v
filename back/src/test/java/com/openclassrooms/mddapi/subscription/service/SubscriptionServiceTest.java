package com.openclassrooms.mddapi.subscription.service;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import com.openclassrooms.mddapi.subscription.entity.Subscription;
import com.openclassrooms.mddapi.subscription.entity.SubscriptionId;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionAlreadyExists;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.subscription.mapper.SubscriptionMapper;
import com.openclassrooms.mddapi.subscription.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Topic topic;

    @BeforeEach
    void setUp() {
        topic = new Topic();
        topic.setId(10L);
        topic.setPublicId("topic-public-id");
        topic.setName("Java");
        topic.setDescription("Java programming");
    }

    @Test
    void subscribe_shouldSaveSubscriptionWhenItDoesNotExist() {
        Long userId = 1L;
        SubscriptionId subscriptionId = new SubscriptionId(userId, topic.getId());

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId)).thenReturn(false);

        subscriptionService.subscribe(topic);

        ArgumentCaptor<Subscription> captor =
                ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionRepository).save(captor.capture());

        Subscription savedSubscription = captor.getValue();

        assertEquals(userId, savedSubscription.getUserId());
        assertEquals(topic.getId(), savedSubscription.getTopicId());
    }

    @Test
    void subscribe_shouldThrowWhenSubscriptionAlreadyExists() {
        Long userId = 1L;
        SubscriptionId subscriptionId = new SubscriptionId(userId, topic.getId());

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId)).thenReturn(true);

        assertThrows(
                SubscriptionAlreadyExists.class,
                () -> subscriptionService.subscribe(topic)
        );

        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void unsubscribe_shouldDeleteSubscriptionWhenItExists() {
        Long userId = 1L;
        SubscriptionId subscriptionId = new SubscriptionId(userId, topic.getId());
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setTopicId(topic.getId());

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId)).thenReturn(true);
        when(subscriptionRepository.getReferenceById(subscriptionId))
                .thenReturn(subscription);

        subscriptionService.unsubscribe(topic);

        verify(subscriptionRepository).getReferenceById(subscriptionId);
        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    void unsubscribe_shouldThrowWhenSubscriptionDoesNotExist() {
        Long userId = 1L;
        SubscriptionId subscriptionId = new SubscriptionId(userId, topic.getId());

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId)).thenReturn(false);

        assertThrows(
                SubscriptionNotFoundException.class,
                () -> subscriptionService.unsubscribe(topic)
        );

        verify(subscriptionRepository, never()).getReferenceById(any());
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    @Test
    void findAll_shouldReturnMappedSubscriptions() {
        Long userId = 1L;

        Subscription subscription1 = new Subscription();
        subscription1.setUserId(userId);
        subscription1.setTopicId(10L);

        Subscription subscription2 = new Subscription();
        subscription2.setUserId(userId);
        subscription2.setTopicId(20L);

        SubscriptionResponseDto dto1 = mock(SubscriptionResponseDto.class);
        SubscriptionResponseDto dto2 = mock(SubscriptionResponseDto.class);

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(subscriptionRepository.findAllByUserId(userId))
                .thenReturn(List.of(subscription1, subscription2));
        when(subscriptionMapper.toDto(subscription1)).thenReturn(dto1);
        when(subscriptionMapper.toDto(subscription2)).thenReturn(dto2);

        List<SubscriptionResponseDto> result = subscriptionService.findAll();

        assertEquals(List.of(dto1, dto2), result);

        verify(subscriptionMapper).toDto(subscription1);
        verify(subscriptionMapper).toDto(subscription2);
    }

    @Test
    void findAll_shouldReturnEmptyListWhenUserHasNoSubscriptions() {
        Long userId = 1L;

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(subscriptionRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<SubscriptionResponseDto> result = subscriptionService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(subscriptionMapper);
    }

    @Test
    void existsByUserIdAndTopicId_shouldReturnTrueWhenSubscriptionExists() {
        Long userId = 1L;
        Long topicId = 10L;

        when(subscriptionRepository.existsByUserIdAndTopicId(userId, topicId))
                .thenReturn(true);

        boolean result = subscriptionService.existsByUserIdAndTopicId(userId, topicId);

        assertTrue(result);
        verify(subscriptionRepository).existsByUserIdAndTopicId(userId, topicId);
    }

    @Test
    void existsByUserIdAndTopicId_shouldReturnFalseWhenSubscriptionDoesNotExist() {
        Long userId = 1L;
        Long topicId = 10L;

        when(subscriptionRepository.existsByUserIdAndTopicId(userId, topicId))
                .thenReturn(false);

        boolean result = subscriptionService.existsByUserIdAndTopicId(userId, topicId);

        assertFalse(result);
        verify(subscriptionRepository).existsByUserIdAndTopicId(userId, topicId);
    }
}
