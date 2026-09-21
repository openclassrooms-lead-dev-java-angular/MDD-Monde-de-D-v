package com.openclassrooms.mddapi.article.repository;

import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsBySlug(String slug);

    Optional<Article> findBySlug(String slug);

    Article getReferenceBySlug(String slug);

    boolean existsBySlugAndAuthor(String slug, User author);
}
