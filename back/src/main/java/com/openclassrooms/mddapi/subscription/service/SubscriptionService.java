package com.openclassrooms.mddapi.subscription.service;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.subscription.mapper.SubscriptionMapper;
import com.openclassrooms.mddapi.subscription.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.subscription.dto.SubscriptionResponseDto;
import com.openclassrooms.mddapi.subscription.entity.Subscription;
import com.openclassrooms.mddapi.subscription.entity.SubscriptionId;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionAlreadyExists;
import com.openclassrooms.mddapi.subscription.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.topic.entity.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for managing user subscriptions to topics.
 *
 * <p>Subscriptions are associated with the currently authenticated user
 * and a topic. This service handles subscription creation, removal and
 * retrieval.</p>
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     *  Subscribes the currently authenticated user to the given topic.
     *
     *  <p>A subscription is created only if the user is not already
     *  subscribed to the topic.</p>
     *
     *  @param topic the topic to subscribe to
     *  @throws SubscriptionAlreadyExists if the user is already subscribed
     *  to the topic
     */
    @Transactional
    public void subscribe(Topic topic) {
        Long currentUserId = userDetailsService.getPrincipalUserId();

        SubscriptionId subscriptionId = new SubscriptionId(currentUserId, topic.getId());

        if (subscriptionRepository.existsById(subscriptionId)) {
            throw new SubscriptionAlreadyExists("Subscription already exists");
        }

        Subscription subscription = new Subscription();
        subscription.setUserId(currentUserId);
        subscription.setTopicId(topic.getId());

        subscriptionRepository.save(subscription);
    }

    /**
     * Removes the subscription of the currently authenticated user
     * to the given topic.
     *
     * @param topic the topic to unsubscribe from
     * @throws SubscriptionNotFoundException if the user is not subscribed
     * to the topic
     */
    @Transactional
    public void unsubscribe(Topic topic) {
        Long currentUserId = userDetailsService.getPrincipalUserId();

        SubscriptionId subscriptionId = new SubscriptionId(currentUserId, topic.getId());

        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new SubscriptionNotFoundException("Subscription not found");
        }

        Subscription subscription = subscriptionRepository.getReferenceById(subscriptionId);

        subscriptionRepository.delete(subscription);
    }

    /**
     * Retrieves all subscriptions of the currently authenticated user.
     *
     * @return the list of the user's subscriptions
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> findAll() {

        Long currentUserId = userDetailsService.getPrincipalUserId();

        return subscriptionRepository
                .findAllByUserId(currentUserId)
                .stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }
}
