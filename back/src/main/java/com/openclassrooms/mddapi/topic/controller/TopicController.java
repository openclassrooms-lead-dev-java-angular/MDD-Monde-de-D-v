package com.openclassrooms.mddapi.topic.controller;

import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.service.TopicService;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * REST controller for managing topics.
 *
 * <p>Provides endpoints for retrieving, creating, updating and subscribing
 * to topics.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/topics")
@Validated
public class TopicController {

    private final TopicService topicService;

    /**
     * Retrieves a paginated list of topics.
     *
     * <p> Url example: GET /api/v1/topics?page=0&size=10&sort=name,asc</p
     *
     * @param pageable pagination and sorting parameters
     * @return a paginated list of topics
     */
    @GetMapping("")
    public Page<TopicResponseDto> findAll(
            Pageable pageable
    ) {
        return topicService.findAll(pageable);
    }

    @GetMapping("/options")
    public List<TopicOptionDto> getTopicOptions() {
        return topicService.getOptions();
    }

    /**
     * Subscribes the current user to a topic.
     *
     * @param slug the slug of the topic
     */
    @PostMapping("/{id}/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(
            @PathVariable String slug
    ) {
        topicService.subscribe(slug);
    }

    /**
     * Unsubscribes the current user from a topic.
     *
     * @param slug the slug of the topic
     */
    @DeleteMapping("/{id}/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(
            @PathVariable String slug
    ) {
        topicService.unsubscribe(slug);
    }
}
