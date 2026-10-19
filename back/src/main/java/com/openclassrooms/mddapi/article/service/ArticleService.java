package com.openclassrooms.mddapi.article.service;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.exception.ArticleSlugAlreadyExists;
import com.openclassrooms.mddapi.article.mapper.ArticleMapper;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.service.TopicService;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing articles.
 *
 * <p>This service handles article creation, retrieval,
 * slug availability checks, topic validation.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final TopicService topicService;
    private final UserService userService;
    private final UserDetailsServiceImpl  userDetailsService;

    @Transactional(readOnly = true)
    public Page<ArticleResponseDto> findFeedArticles(Pageable pageable) {
        Long userId = userDetailsService.getPrincipalUserId();

        return articleRepository
                .findArticlesByUserSubscriptions(userId, pageable)
                .map(articleMapper::toDto);
    }

    /**
     * Retrieves an article by its slug.
     *
     * @param slug the slug of the article to retrieve
     * @return the article matching the given slug
     * @throws ArticleNotFoundException if no article matches the given slug
     */
    @Transactional(readOnly = true)
    public ArticleResponseDto findBySlug(final String slug) {
        return articleRepository
                .findBySlug(slug)
                .map(articleMapper::toDto)
                .orElseThrow(() -> new ArticleNotFoundException("Article not found with slug : " + slug));
    }

    /**
     * Creates a new article.
     *
     * <p>The method verifies that the slug is available and that the
     * specified topic exists. It then retrieves the currently authenticated
     * user as the author, maps the request DTO to an entity, and persists the article.</p>
     *
     * @param articleRequestDto the data of the article to create
     * @return the created article
     * @throws ArticleSlugAlreadyExists if the article slug already exists * @throws TopicNotFoundException if the specified topic does not exist
     */
    @Transactional
    public ArticleResponseDto create(
            final ArticleRequestDto articleRequestDto
    ) {
        if (articleRepository.existsBySlug(articleRequestDto.slug())) {
            throw new ArticleSlugAlreadyExists("Slug already exists : " + articleRequestDto.slug());
        }

        checkTopicSlug(articleRequestDto.topicSlug());
        Topic topic = topicService.loadBySlug(articleRequestDto.topicSlug());
        User author = userService.loadCurrentUserAuthor();

        Article article = articleMapper.toEntity(articleRequestDto);
        article.setAuthor(author);
        article.setTopic(topic);

        Article savedArticle = articleRepository.save(article);

        log.info("Saved article {}", savedArticle.getSlug());

        return articleMapper.toDto(savedArticle);
    }

    /**
     * Checks whether an article slug is available.
     *
     * @param slug the slug to check
     * @return an {@link AvailableSlugDto} indicating whether the slug is available
     */
    @Transactional(readOnly = true)
    public AvailableSlugDto findAvailableSlug(final String slug) {
        boolean availableSlug = articleRepository.existsBySlug(slug);

        return new AvailableSlugDto(!availableSlug);
    }

    /**
     * Checks whether a topic exists for the given slug.
     *
     * @param topicSlug the topic slug to check
     * @throws TopicNotFoundException if no topic matches the given slug
     */
    @Transactional(readOnly = true)
    private void checkTopicSlug(final String topicSlug) {
        if (!topicService.existsBySlug(topicSlug)) {
            throw new TopicNotFoundException("Topic not found with slug : " + topicSlug);
        }
    }

    @Transactional(readOnly = true)
    public Article loadArticleBySlug(final String slug) {
        return articleRepository.getReferenceBySlug(slug);
    }
}
