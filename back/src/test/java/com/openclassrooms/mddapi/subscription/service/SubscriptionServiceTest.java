package com.openclassrooms.mddapi.subscription.service;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import com.openclassrooms.mddapi.subscription.entity.Subscription;
import com.openclassrooms.mddapi.subscription.entity.SubscriptionId;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionAlreadyExists;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.subscription.mapper.SubscriptionMapper;
import com.openclassrooms.mddapi.subscription.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    @Test
    void shouldSubscribeToTopic() {
        Long userId = 1L;
        Long topicId = 10L;
        Topic topic = new Topic();
        topic.setId(topicId);

        SubscriptionId subscriptionId = new SubscriptionId(userId, topicId);

        when(userDetailsService.getPrincipalUserId())
                .thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId))
                .thenReturn(false);

        subscriptionService.subscribe(topic);

        verify(userDetailsService).getPrincipalUserId();
        verify(subscriptionRepository).existsById(subscriptionId);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionRepository).save(captor.capture());

        Subscription subscription = captor.getValue();

        assertThat(subscription.getUserId()).isEqualTo(userId);
        assertThat(subscription.getTopicId()).isEqualTo(topicId);
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionAlreadyExists() {
        Long userId = 1L;
        Long topicId = 10L;

        Topic topic = new Topic();
        topic.setId(topicId);

        SubscriptionId subscriptionId = new SubscriptionId(userId, topicId);

        when(userDetailsService.getPrincipalUserId())
                .thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId))
                .thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.subscribe(topic))
                .isInstanceOf(SubscriptionAlreadyExists.class)
                .hasMessage("Subscription already exists");

        verify(subscriptionRepository)
                .existsById(subscriptionId);
        verify(subscriptionRepository, never())
                .save(any());
    }

    @Test
    void shouldUnsubscribeFromTopic() {
        Long userId = 1L;
        Long topicId = 10L;

        Topic topic = new Topic();
        topic.setId(topicId);

        SubscriptionId subscriptionId = new SubscriptionId(userId, topicId);
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setTopicId(topicId);

        when(userDetailsService.getPrincipalUserId())
                .thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId))
                .thenReturn(true);
        when(subscriptionRepository.getReferenceById(subscriptionId))
                .thenReturn(subscription);

        subscriptionService.unsubscribe(topic);

        verify(userDetailsService)
                .getPrincipalUserId();
        verify(subscriptionRepository)
                .existsById(subscriptionId);
        verify(subscriptionRepository)
                .getReferenceById(subscriptionId);
        verify(subscriptionRepository)
                .delete(subscription);
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionDoesNotExist() {
        Long userId = 1L;
        Long topicId = 10L;

        Topic topic = new Topic();
        topic.setId(topicId);

        SubscriptionId subscriptionId = new SubscriptionId(userId, topicId);

        when(userDetailsService.getPrincipalUserId())
                .thenReturn(userId);
        when(subscriptionRepository.existsById(subscriptionId))
                .thenReturn(false);

        assertThatThrownBy(() -> subscriptionService.unsubscribe(topic))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessage("Subscription not found");

        verify(subscriptionRepository).existsById(subscriptionId);
        verify(subscriptionRepository, never()).getReferenceById(any());
        verify(subscriptionRepository, never()).delete(any());
    }

    @Test
    void shouldReturnCurrentUserSubscriptions() {
        Long userId = 1L;

        List<TopicResponseDto> topics = TopicTestFactory.createTopicResponseDtoList();

        Subscription subscription1 = new Subscription();
        subscription1.setUserId(userId);
        subscription1.setTopicId(10L);

        SubscriptionResponseDto response1 = new SubscriptionResponseDto(topics.get(0), LocalDateTime.now());

        Subscription subscription2 = new Subscription();
        subscription2.setUserId(userId);
        subscription2.setTopicId(20L);

        SubscriptionResponseDto response2 = new SubscriptionResponseDto(topics.get(1), LocalDateTime.now());

        when(userDetailsService.getPrincipalUserId())
                .thenReturn(userId);
        when(subscriptionRepository.findAllByUserId(userId))
                .thenReturn(List.of(subscription1, subscription2));
        when(subscriptionMapper.toDto(subscription1))
                .thenReturn(response1);
        when(subscriptionMapper.toDto(subscription2))
                .thenReturn(response2);

        List<SubscriptionResponseDto> result = subscriptionService.findAll();

        assertThat(result).isEqualTo(List.of(response1, response2));

        verify(userDetailsService).getPrincipalUserId();
        verify(subscriptionRepository).findAllByUserId(userId);
        verify(subscriptionMapper).toDto(subscription1);
        verify(subscriptionMapper).toDto(subscription2);
    }
}
