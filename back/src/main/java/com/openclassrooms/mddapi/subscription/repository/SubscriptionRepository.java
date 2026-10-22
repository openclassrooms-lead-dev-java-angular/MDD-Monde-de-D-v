package com.openclassrooms.mddapi.subscription.repository;

import com.openclassrooms.mddapi.subscription.entity.Subscription;
import com.openclassrooms.mddapi.subscription.entity.SubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

    List<Subscription> findAllByUserId(Long userId);

    @Query(""" 
        SELECT s.topicId 
        FROM Subscription s 
        WHERE s.userId = :userId 
                AND s.topicId IN :topicIds 
    """)
    Set<Long> findSubscribedTopicIds(
            @Param("userId") Long userId,
            @Param("topicIds") Collection<Long> topicIds
    );

    boolean existsByUserIdAndTopicId(Long userId, Long topicId);
}
