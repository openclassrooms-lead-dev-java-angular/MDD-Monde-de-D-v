package com.openclassrooms.mddapi.article.service;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.AticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.exception.ArticleSlugAlreadyExists;
import com.openclassrooms.mddapi.article.mapper.ArticleMapper;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.storage.service.StorageService;
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

    @Transactional(readOnly = true)
    public Page<ArticleResponseDto> findAll(Pageable pageable) {
        return articleRepository
                .findAll(pageable)
                .map(articleMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ArticleResponseDto findBySlug(final String slug) {
        return articleRepository
                .findBySlug(slug)
                .map(articleMapper::toDto)
                .orElseThrow(() -> new ArticleNotFoundException("Article not found with slug : " + slug));
    }

    @Transactional
    public ArticleResponseDto create(final ArticleRequestDto articleRequestDto) {
        if (articleRepository.existsBySlug(articleRequestDto.slug())) {
            throw new ArticleSlugAlreadyExists("Slug already exists : " + articleRequestDto.slug());
        }

        checkTopicSlug(articleRequestDto.topicSlug());

        User author = userService.loadCurrentUserAuthor();

        Article article = articleMapper.toEntity(articleRequestDto);
        article.setAuthor(author);

        // media upload
        if (articleRequestDto.media() != null) {

            String filename = storageService.upload(articleRequestDto.media(), resourceType, articleRequestDto.slug());
            article.setMedia(filename);
        }

        Article savedArticle = articleRepository.save(article);

        log.info("Saved article {}", savedArticle.getSlug());

        return articleMapper.toDto(savedArticle);
    }

    @Transactional
    public ArticleResponseDto update(
            final String slug,
            final AticleUpdateRequestDto articleRequestDto
    ) {
        if (articleRepository.existsBySlug(articleRequestDto.slug())) {
            throw new ArticleSlugAlreadyExists("Slug already exists : " + slug);
        }

        if (
                !slug.equals(articleRequestDto.slug())
                        && !articleRepository.existsBySlug(slug)
        ) {
            throw new ArticleNotFoundException("Article not found with slug : " + slug);
        }

        checkTopicSlug(articleRequestDto.topicSlug());

        boolean isMediaUploaded = articleRequestDto.media() != null
                && articleRequestDto.updatedMedia();

        Article article = articleRepository.getReferenceBySlug(slug);
        String oldFilename = article.getMedia();
        articleMapper.updateEntity(articleRequestDto, article);

        // media upload
        if (isMediaUploaded) {
            String filename = storageService.upload(
                    articleRequestDto.media(),
                    resourceType,
                    articleRequestDto.slug()
            );

            article.setMedia(filename);
        }

        Article savedArticle = articleRepository.save(article);

        if (isMediaUploaded) {
            storageService.delete(oldFilename);
        }

        log.info("Uodated article {}", savedArticle);

        return articleMapper.toDto(savedArticle);
    }

    @Transactional(readOnly = true)
    public AvailableSlugDto findAvailableSlug(String slug) {
        boolean availableSlug = articleRepository.existsBySlug(slug);

        return new AvailableSlugDto(!availableSlug);
    }

    @Transactional(readOnly = true)
    private void checkTopicSlug(String topicSlug) {
        if (topicService.existsBySlug(topicSlug)) {
            throw new TopicNotFoundException("Topic not found with slug : " + topicSlug);
        }
    }
}
