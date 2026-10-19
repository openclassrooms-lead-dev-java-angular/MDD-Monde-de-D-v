package com.openclassrooms.mddapi.topic.repository;

import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsBySlug(String slug);

    Optional<Topic> findBySlug(String slug);

    Topic getReferenceBySlug(String slug);

    @Query("""
        SELECT new com.openclassrooms.mddapi.topic.dto.TopicOptionDto(
            t.name, 
            t.slug
        )
        FROM Topic t
        ORDER BY t.name ASC
        """)
    List<TopicOptionDto> findAllTopicOptions();
}
