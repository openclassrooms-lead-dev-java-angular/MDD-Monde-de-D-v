package com.openclassrooms.mddapi.subscription.entity;

import com.openclassrooms.mddapi.topic.entity.Topic;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@IdClass(SubscriptionId.class)
public class Subscription {

    @Id
    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Id
    @Column(
            name = "topic_id",
            nullable = false
    )
    private Long topicId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "topic_id",
            insertable = false,
            updatable = false
    )
    private Topic topic;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
