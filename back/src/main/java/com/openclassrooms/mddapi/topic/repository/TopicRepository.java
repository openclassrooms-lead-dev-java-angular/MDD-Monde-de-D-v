package com.openclassrooms.mddapi.topic.repository;

import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
