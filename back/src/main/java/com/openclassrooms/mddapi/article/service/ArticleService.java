package com.openclassrooms.mddapi.article.service;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.ArticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.exception.ArticleSlugAlreadyExists;
import com.openclassrooms.mddapi.article.mapper.ArticleMapper;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.storage.service.StorageService;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.service.TopicService;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for managing articles.
 *
 * <p>This service handles article creation, update, retrieval,
 * slug availability checks, topic validation, and media storage.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final StorageService storageService;
    private final TopicService topicService;
    private final UserService userService;

    @Value("${app.storage-path.article}")
    private String resourceType;

    @Value("${app.media-url}")
    private String mediaUrl;

    /**
     * Retrieves a paginated list of articles.
     *
     * <p>The retrieved articles are mapped to
     * {@link ArticleResponseDto} objects.</p>
     *
     * @param pageable pagination and sorting parameters
     * @return a page containing the articles
     */
    @Transactional(readOnly = true)
    public Page<ArticleResponseDto> findAll(Pageable pageable) {
        return articleRepository
                .findAll(pageable)
                .map(article -> articleMapper.toDto(article, mediaUrl));
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
                .map(article -> articleMapper.toDto(article, mediaUrl))
                .orElseThrow(() -> new ArticleNotFoundException("Article not found with slug : " + slug));
    }

    /**
     * Creates a new article.
     *
     * <p>The method verifies that the slug is available and that the
     * specified topic exists. It then retrieves the currently authenticated
     * user as the author, maps the request DTO to an entity, optionally
     * uploads the associated media, and persists the article.</p>
     *
     * @param articleRequestDto the data of the article to create
     * @param media             the optional media file associated with the article
     * @return the created article
     * @throws ArticleSlugAlreadyExists if the article slug already exists * @throws TopicNotFoundException if the specified topic does not exist
     */
    @Transactional
    public ArticleResponseDto create(
            final ArticleRequestDto articleRequestDto,
            final MultipartFile media
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

        // media upload
        if (media != null) {
            String filename = storageService.upload(media, resourceType, articleRequestDto.slug());
            article.setMedia(filename);
        }

        Article savedArticle = articleRepository.save(article);

        log.info("Saved article {}", savedArticle.getSlug());

        return articleMapper.toDto(savedArticle, mediaUrl);
    }

    /**
     * Updates an existing article.
     *
     * <p>The current slug is used to identify the article to update.
     * If the slug is changed, the new slug must not already be used by
     * another article. The specified topic must also exist.</p>
     *
     * <p>If a new media file is provided and {@code updatedMedia} is
     * {@code true}, the new file is uploaded and the previous media
     * file is deleted after the article has been saved.</p>
     *
     * @param slug              the current slug of the article
     * @param articleRequestDto the updated article data
     * @return the updated article
     * @throws ArticleSlugAlreadyExists if the new slug is already used
     * @throws ArticleNotFoundException if the article does not exist
     * @throws TopicNotFoundException   if the specified topic does not exist
     */
    @Transactional
    public ArticleResponseDto update(
            final String slug,
            final ArticleUpdateRequestDto articleRequestDto,
            MultipartFile media
    ) {
        if (articleRepository.existsBySlug(articleRequestDto.slug())) {
            throw new ArticleSlugAlreadyExists("Slug already exists : " + slug);
        }

        if (!slug.equals(articleRequestDto.slug())
                && !articleRepository.existsBySlug(slug)
        ) {
            throw new ArticleNotFoundException("Article not found with slug : " + slug);
        }

        checkTopicSlug(articleRequestDto.topicSlug());

        boolean isMediaUploaded = media != null
                && articleRequestDto.updatedMedia();

        Article article = articleRepository.getReferenceBySlug(slug);
        String oldFilename = article.getMedia();
        articleMapper.updateEntity(articleRequestDto, article);

        // media upload
        if (isMediaUploaded) {
            String filename = storageService.upload(
                    media,
                    resourceType,
                    articleRequestDto.slug()
            );

            article.setMedia(filename);
        }

        Article savedArticle = articleRepository.save(article);

        if (isMediaUploaded) {
            storageService.delete(oldFilename);
        }

        log.info("Uodated article with slug {}", savedArticle.getSlug());

        return articleMapper.toDto(savedArticle, mediaUrl);
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
