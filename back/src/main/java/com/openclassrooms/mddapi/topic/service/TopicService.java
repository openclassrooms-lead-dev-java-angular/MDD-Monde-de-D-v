package com.openclassrooms.mddapi.topic.service;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.subscription.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.subscription.service.SubscriptionService;
import com.openclassrooms.mddapi.topic.dto.TopicRequestDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.exception.TopicSlugAlreadyExists;
import com.openclassrooms.mddapi.topic.mapper.TopicMapper;
import com.openclassrooms.mddapi.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Service responsible for managing topics.
 *
 * <p>Handles topic retrieval, creation, update and slug availability checks.</p>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;
    private final SubscriptionService subscriptionService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Retrieves a topic by its slug.
     *
     * @param slug the topic slug
     * @return the topic response
     * @throws TopicNotFoundException if no topic matches the given slug
     */
    @Transactional(readOnly = true)
    public TopicResponseDto findBySlug(String slug) {
        Topic topic = topicRepository
                .findBySlug(slug)
                .orElseThrow(TopicNotFoundException::new);

        Long userId = userDetailsServiceImpl.getPrincipalUserId();
        boolean isSubscribedTopic = subscriptionRepository.existsByUserIdAndTopicId(userId, topic.getId());

        return topicMapper.toDto(topic, isSubscribedTopic);

    }

    /**
     * Retrieves all topics using the given pagination and sorting parameters.
     *
     * @param pageable pagination and sorting parameters
     * @return a paginated list of topic responses
     */
    @Transactional(readOnly = true)
    public Page<TopicResponseDto> findAll(Pageable pageable) {
        Page<Topic> topics = topicRepository.findAll(pageable);

        if (topics.isEmpty()) {
            return Page.empty(pageable);
        }

        Long userId = userDetailsServiceImpl.getPrincipalUserId();

        List<Long> topicIds = topics.getContent().stream().map(Topic::getId).toList();

        Set<Long> subscribedTopicIds = subscriptionRepository.findSubscribedTopicIds(userId, topicIds);

        return topics.map(topic -> topicMapper.toDto(
                topic,
                subscribedTopicIds.contains(topic.getId())
        ));
    }

    /**
     * Creates a new topic.
     *
     * @param topicRequestDto the data used to create the topic
     * @return the created topic response
     * @throws TopicSlugAlreadyExists if the requested slug is already in use
     */
    @Transactional
    public TopicResponseDto create(TopicRequestDto topicRequestDto) {

        if (topicRepository.existsBySlug(topicRequestDto.slug())) {
            throw new TopicSlugAlreadyExists("Slug already exists");
        }

        Topic topic = topicMapper.toEntity(topicRequestDto);

        log.info("Creating topic with slug '{}'", topicRequestDto.slug());



        return topicMapper.toDto(topicRepository.save(topic), false);
    }

    /**
     * Updates an existing topic identified by its slug.
     *
     * @param slug            the slug of the topic to update
     * @param topicRequestDto the data used to update the topic
     * @return the updated topic response * @throws TopicNotFoundException if no topic matches the given slug
     */
    @Transactional
    public TopicResponseDto update(String slug, TopicRequestDto topicRequestDto) {

        if (!topicRepository.existsBySlug(slug)) {
            throw new TopicNotFoundException("Topic not found");
        }

        Topic topic = topicRepository
                .getReferenceBySlug(slug);

        topicMapper.updateEntity(topic, topicRequestDto);

        log.info("Updating topic with slug '{}'", slug);

        Long userId = userDetailsServiceImpl.getPrincipalUserId();
        boolean isSubscribedTopic = subscriptionRepository.existsByUserIdAndTopicId(userId, topic.getId());

        return topicMapper.toDto(topicRepository.save(topic),  isSubscribedTopic);
    }

    /**
     * Subscribes the current user to a topic.
     *
     * @param slug the topic slug
     */
    @Transactional
    public void subscribe(String slug) {
        if (!topicRepository.existsBySlug(slug)) {
            throw new TopicNotFoundException("Topic not found with slug '" + slug + "'");
        }

        Topic topic = topicRepository.getReferenceBySlug(slug);

        subscriptionService.subscribe(topic);
    }

    /**
     * Unsubscribes the current user from a topic.
     *
     * @param slug the topic slug
     */
    @Transactional
    public void unsubscribe(String slug) {
        if (!topicRepository.existsBySlug(slug)) {
            throw new TopicNotFoundException("Topic not found with slug '" + slug + "'");
        }

        Topic topic = topicRepository.getReferenceBySlug(slug);

        subscriptionService.unsubscribe(topic);
    }

    /**
     * Checks whether a topic slug is available.
     *
     * @param slug the slug to check
     * @return a response indicating whether the slug is available
     */
    @Transactional(readOnly = true)
    public AvailableSlugDto availableSlug(String slug) {
        boolean exists = topicRepository.existsBySlug(slug);

        return new AvailableSlugDto(!exists);
    }

    /**
     * Checks whether a topic exists with the given slug.
     *
     * @param slug the slug of the topic to check
     * @return {@code true} if a topic with the given slug exists,
     * {@code false} otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return topicRepository.existsBySlug(slug);
    }

    /**
     * Loads a topic reference by its slug.
     *
     * <p>loaded from the database immediately and can trigger a database access when
     * its properties are accessed.</p>
     *
     * @param slug the slug of the topic to load
     * @return a reference to the topic identified by the given slug
     * @throws jakarta.persistence.EntityNotFoundException if the topic does not
     *                                                     exist when the reference is accessed
     */
    @Transactional(readOnly = true)
    public Topic loadBySlug(String slug) {
        return topicRepository.getReferenceBySlug(slug);
    }
}
