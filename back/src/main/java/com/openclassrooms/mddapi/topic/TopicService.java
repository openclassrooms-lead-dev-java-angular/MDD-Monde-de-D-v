package com.openclassrooms.mddapi.topic;

import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.topic.dto.TopicRequestDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.exception.TopicSlugAlreadyExists;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

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

    /**
     * Retrieves a topic by its slug.
     *
     * @param slug the topic slug
     * @return the topic response
     * @throws TopicNotFoundException if no topic matches the given slug
     */
    @Transactional(readOnly = true)
    public TopicResponseDto findBySlug(String slug) {
        return topicRepository
                .findBySlug(slug)
                .map(topicMapper::toDto)
                .orElseThrow(TopicNotFoundException::new);
    }

    /**
     * Retrieves all topics using the given pagination and sorting parameters.
     *
     * @param pageable pagination and sorting parameters
     * @return a paginated list of topic responses
     */
    @Transactional(readOnly = true)
    public Page<TopicResponseDto> findAll(Pageable pageable) {
        return topicRepository
                .findAll(pageable)
                .map(topicMapper::toDto);
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

        return topicMapper.toDto(topicRepository.save(topic));
    }

    /**
     * Updates an existing topic identified by its slug.
     *
     * @param slug the slug of the topic to update
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

        return topicMapper.toDto(topicRepository.save(topic));
    }

    /**
     * Subscribes the current user to a topic.
     *
     * @param slug the topic slug
     */
    @Transactional
    public void subscribe(String slug) {
        // todo in git subscription branch
    }

    /**
     * Unsubscribes the current user from a topic.
     *
     * @param slug the topic slug
     */
    @Transactional
    public void unsubscribe(String slug) {
        // todo in git subscription branch
    }

    /**
     * Checks whether a topic slug is available.
     *
     * @param slug the slug to check
     * @return a response indicating whether the slug is available
     */
    @Transactional(readOnly = true)
    public AvailableSlugDto availableSlug(String slug) {
        boolean  exists = topicRepository.existsBySlug(slug);

        return new AvailableSlugDto(!exists);
    }
}
