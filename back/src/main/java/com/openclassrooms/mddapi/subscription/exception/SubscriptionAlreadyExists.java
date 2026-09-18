package com.openclassrooms.mddapi.subscription.exception;

public class SubscriptionAlreadyExists extends RuntimeException {
    public SubscriptionAlreadyExists(String message) {
        super(message);
    }
}
