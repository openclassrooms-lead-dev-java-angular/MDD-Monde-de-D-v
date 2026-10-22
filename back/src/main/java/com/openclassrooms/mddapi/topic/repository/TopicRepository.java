package com.openclassrooms.mddapi.topic.repository;

import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsBySlug(String slug);

    Topic getReferenceBySlug(String slug);

    @Query("""
        SELECT new com.openclassrooms.mddapi.topic.dto.TopicOptionDto(
            t.name,\s
            t.slug
        )
        FROM Topic t
        ORDER BY t.name ASC
       \s""")
    List<TopicOptionDto> findAllTopicOptions();

    @Query("""
    SELECT t
    FROM Topic t
    WHERE t.id IN (
        SELECT s.topicId
        FROM Subscription s
        WHERE s.userId = :userId
    )
    """)
    Page<Topic> findSubscribedTopics(
            Pageable pageable,
            @Param("userId") Long userId
    );
}
