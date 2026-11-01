
package com.openclassrooms.mddapi.subscription.mapper;

import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import com.openclassrooms.mddapi.subscription.entity.Subscription;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionMapperTest {

    private final SubscriptionMapper subscriptionMapper =
            Mappers.getMapper(SubscriptionMapper.class);

    @Test
    void toDto_shouldReturnNullWhenSubscriptionIsNull() {
        SubscriptionResponseDto result = subscriptionMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_shouldMapSubscriptionToDto() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 30, 9, 0);

        Topic topic = new Topic();

        Subscription subscription = Subscription.builder()
                .userId(1L)
                .topicId(2L)
                .topic(topic)
                .createdAt(createdAt)
                .build();

        SubscriptionResponseDto result = subscriptionMapper.toDto(subscription);

        assertThat(result).isNotNull();
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.topic()).isNotNull();
    }

    @Test
    void toDto_shouldMapNullTopic() {
        Subscription subscription = Subscription.builder()
                .userId(1L)
                .topicId(2L)
                .topic(null)
                .createdAt(LocalDateTime.of(2026, 10, 30, 9, 0))
                .build();

        SubscriptionResponseDto result = subscriptionMapper.toDto(subscription);

        assertThat(result).isNotNull();
        assertThat(result.topic()).isNull();
        assertThat(result.createdAt()).isEqualTo(subscription.getCreatedAt());
    }

    @Test
    void toDto_shouldMapNullCreatedAt() {
        Subscription subscription = Subscription.builder()
                .userId(1L)
                .topicId(2L)
                .topic(new Topic())
                .createdAt(null)
                .build();

        SubscriptionResponseDto result = subscriptionMapper.toDto(subscription);

        assertThat(result).isNotNull();
        assertThat(result.createdAt()).isNull();
    }

    @Test
    void toDto_shouldMapEmptySubscription() {
        Subscription subscription = new Subscription();

        SubscriptionResponseDto result = subscriptionMapper.toDto(subscription);

        assertThat(result).isNotNull();
        assertThat(result.topic()).isNull();
        assertThat(result.createdAt()).isNull();
    }
}
