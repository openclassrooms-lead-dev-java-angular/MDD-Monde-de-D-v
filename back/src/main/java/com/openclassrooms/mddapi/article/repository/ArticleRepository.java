package com.openclassrooms.mddapi.article.repository;

import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsBySlug(String slug);

    Optional<Article> findBySlug(String slug);

    Article getReferenceBySlug(String slug);

    boolean existsBySlugAndAuthor(String slug, User author);

    Page<Article> findByTopic(Topic topic, Pageable pageable);

    @Query("""
        SELECT a
         FROM Article a
         WHERE a.topic.id IN (
             SELECT s.topicId
             FROM Subscription s
             WHERE s.userId = :userId
        )
        ORDER BY a.createdAt DESC
        """)
    Page<Article> findArticlesByUserSubscriptions(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
