package com.openclassrooms.mddapi.subscription.repository;

import com.openclassrooms.mddapi.subscription.entity.Subscription;
import com.openclassrooms.mddapi.subscription.entity.SubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId>  {

    List<Subscription> findAllByUserId(Long userId);
}
