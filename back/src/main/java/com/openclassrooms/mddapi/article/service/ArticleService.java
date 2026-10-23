package com.openclassrooms.mddapi.article.service;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.mapper.ArticleMapper;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
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
 * topic validation.</p>
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
     * Retrieves an article by its public id.
     *
     * @param publicId the public id of the article to retrieve
     * @return the article matching the given public id
     * @throws ArticleNotFoundException if no article matches the given public id
     */
    @Transactional(readOnly = true)
    public ArticleResponseDto findByPublicId(final String publicId) {
        return articleRepository
                .findByPublicId(publicId)
                .map(articleMapper::toDto)
                .orElseThrow(() -> new ArticleNotFoundException("Article not found with id : " + publicId));
    }

    /**
     * Creates a new article.
     *
     * <p>The method verifies that the public id is available and that the
     * specified topic exists. It then retrieves the currently authenticated
     * user as the author, maps the request DTO to an entity, and persists the article.</p>
     *
     * @param articleRequestDto the data of the article to create
     * @return the created article
     */
    @Transactional
    public ArticleResponseDto create(
            final ArticleRequestDto articleRequestDto
    ) {

        checkTopicPublicId(articleRequestDto.topicPublicId());
        Topic topic = topicService.loadByPublicId(articleRequestDto.topicPublicId());
        User author = userService.loadCurrentUserAuthor();

        Article article = articleMapper.toEntity(articleRequestDto);
        article.setAuthor(author);
        article.setTopic(topic);

        Article savedArticle = articleRepository.save(article);

        log.info("Saved article {}", savedArticle.getPublicId());

        return articleMapper.toDto(savedArticle);
    }

    /**
     * Checks whether a topic exists for the given public id.
     *
     * @param topicPublicId the topic public id to check
     * @throws TopicNotFoundException if no topic matches the given public id
     */
    @Transactional(readOnly = true)
    private void checkTopicPublicId(final String topicPublicId) {
        if (!topicService.existsByPublicId(topicPublicId)) {
            throw new TopicNotFoundException("Topic not found with public id : " + topicPublicId);
        }
    }

    @Transactional(readOnly = true)
    public Article loadArticleByPublicId(final String publicId) {
        return articleRepository.getReferenceByPublicId(publicId);
    }
}
