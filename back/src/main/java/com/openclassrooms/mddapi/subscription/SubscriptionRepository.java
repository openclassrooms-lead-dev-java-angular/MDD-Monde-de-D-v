package com.openclassrooms.mddapi.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId>  {

    List<Subscription> findAllByUserId(Long userId);
}
