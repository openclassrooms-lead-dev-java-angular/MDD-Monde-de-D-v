package com.openclassrooms.mddapi.topic.repository;

import com.openclassrooms.mddapi.topic.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsBySlug(String slug);

    Optional<Topic> findBySlug(String slug);

    Topic getReferenceBySlug(String slug);
}
