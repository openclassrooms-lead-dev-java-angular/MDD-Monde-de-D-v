package com.openclassrooms.mddapi.topic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsBySlug(String slug);

    Optional<Topic> findBySlug(String slug);

    Topic getReferenceBySlug(String slug);
}
