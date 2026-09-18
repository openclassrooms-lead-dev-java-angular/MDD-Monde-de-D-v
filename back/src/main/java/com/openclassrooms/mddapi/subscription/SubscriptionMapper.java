package com.openclassrooms.mddapi.subscription;

import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponseDto toDto(Subscription subscription);
}
