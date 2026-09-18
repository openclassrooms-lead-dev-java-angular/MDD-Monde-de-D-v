package com.openclassrooms.mddapi.subscription.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SubscriptionId implements Serializable {

    private Long userId;

    private Long topicId;
}
