package com.openclassrooms.mddapi.topic.exception;

public class TopicNotFoundException extends RuntimeException {

    public TopicNotFoundException(String message) {

        super(message);
    }

    public TopicNotFoundException() {

        super("Topic not found");
    }
}
