package com.openclassrooms.mddapi.topic.exception;

public class TopicSlugAlreadyExists extends RuntimeException {

    public TopicSlugAlreadyExists(String message) {

        super(message);
    }
}
