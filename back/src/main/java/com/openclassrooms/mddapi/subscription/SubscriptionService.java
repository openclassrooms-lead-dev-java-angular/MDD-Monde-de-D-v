package com.openclassrooms.mddapi.subscription;

import com.openclassrooms.mddapi.auth.service.AuthService;
import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionAlreadyExists;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.topic.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final AuthService authService;

    @Transactional
    public void subscribe(Topic topic) {
        Long currentUserId = authService.getPrincipalUserId();

        SubscriptionId subscriptionId = new SubscriptionId(currentUserId, topic.getId());

        if (subscriptionRepository.existsById(subscriptionId)) {
            throw new SubscriptionAlreadyExists("Subscription already exists");
        }

        Subscription subscription = new Subscription();
        subscription.setUserId(currentUserId);
        subscription.setTopicId(topic.getId());

        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(Topic topic) {
        Long currentUserId = authService.getPrincipalUserId();

        SubscriptionId subscriptionId = new SubscriptionId(currentUserId, topic.getId());

        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new SubscriptionNotFoundException("Subscription not found");
        }

        Subscription subscription = subscriptionRepository.getReferenceById(subscriptionId);

        subscriptionRepository.delete(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> findAll() {

        Long currentUserId = authService.getPrincipalUserId();

        return subscriptionRepository
                .findAllByUserId(currentUserId)
                .stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }
}
