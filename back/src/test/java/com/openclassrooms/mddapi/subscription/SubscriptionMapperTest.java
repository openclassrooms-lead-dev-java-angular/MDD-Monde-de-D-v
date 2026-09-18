package com.openclassrooms.mddapi.subscription;

import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import com.openclassrooms.mddapi.topic.Topic;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubscriptionMapperTest {

    private final SubscriptionMapper subscriptionMapper = Mappers.getMapper(SubscriptionMapper.class);

    @Test
    void shouldMapSubscriptionToDto() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 18, 10, 0);

        Topic topic = TopicTestFactory.createTopic();

        Subscription subscription = new Subscription();
        subscription.setUserId(10L);
        subscription.setTopicId(1L);
        subscription.setTopic(topic);
        subscription.setCreatedAt(createdAt);
        SubscriptionResponseDto result = subscriptionMapper.toDto(subscription);
        assertThat(result).isNotNull();
        assertThat(result.topic()).isNotNull();
        assertThat(result.topic().id()).isEqualTo(1L);
        assertThat(result.topic().name()).isEqualTo("Java");
        assertThat(result.topic().slug()).isEqualTo("java");
        assertThat(result.topic().description()).isEqualTo("Java development");
        assertThat(result.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldReturnNullWhenSubscriptionIsNull() {
        SubscriptionResponseDto result = subscriptionMapper.toDto(null);
        assertThat(result).isNull();
    }
}
