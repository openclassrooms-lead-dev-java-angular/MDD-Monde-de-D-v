package com.openclassrooms.mddapi.subscription.mapper;

import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import com.openclassrooms.mddapi.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponseDto toDto(Subscription subscription);
}
