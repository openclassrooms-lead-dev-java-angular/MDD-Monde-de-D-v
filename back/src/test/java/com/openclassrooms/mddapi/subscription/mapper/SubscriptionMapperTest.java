package com.openclassrooms.mddapi.subscription.mapper;

import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SubscriptionMapperTest {

    private final SubscriptionMapper subscriptionMapper =
            Mappers.getMapper(SubscriptionMapper.class);

    @Test
    void toDto_shouldReturnNullWhenSubscriptionIsNull() {
        SubscriptionResponseDto result = subscriptionMapper.toDto(null);

        assertThat(result).isNull();
    }
}