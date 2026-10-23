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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private TopicService topicService;

    @Mock
    private UserService userService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void findFeedArticles_shouldReturnMappedArticles() {
        Long userId = 42L;
        var pageable = PageRequest.of(0, 10);

        Article article = new Article();
        ArticleResponseDto dto = mock(ArticleResponseDto.class);

        Page<Article> articlePage = new PageImpl<>(List.of(article));

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(articleRepository.findArticlesByUserSubscriptions(userId, pageable))
                .thenReturn(articlePage);
        when(articleMapper.toDto(article)).thenReturn(dto);

        Page<ArticleResponseDto> result =
                articleService.findFeedArticles(pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().getFirst());

        verify(articleRepository)
                .findArticlesByUserSubscriptions(userId, pageable);
        verify(articleMapper).toDto(article);
    }

    @Test
    void findFeedArticles_shouldReturnEmptyPageWhenNoArticlesExist() {
        Long userId = 42L;
        var pageable = PageRequest.of(0, 10);

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(articleRepository.findArticlesByUserSubscriptions(userId, pageable))
                .thenReturn(Page.empty(pageable));

        Page<ArticleResponseDto> result =
                articleService.findFeedArticles(pageable);

        assertTrue(result.isEmpty());
        verify(articleMapper, never()).toDto(any(Article.class));
    }

    @Test
    void findByPublicId_shouldReturnMappedArticleWhenFound() {
        String publicId = "article-public-id";
        Article article = new Article();
        ArticleResponseDto dto = mock(ArticleResponseDto.class);

        when(articleRepository.findByPublicId(publicId))
                .thenReturn(Optional.of(article));
        when(articleMapper.toDto(article)).thenReturn(dto);

        ArticleResponseDto result =
                articleService.findByPublicId(publicId);

        assertSame(dto, result);
        verify(articleMapper).toDto(article);
    }

    @Test
    void findByPublicId_shouldThrowWhenArticleDoesNotExist() {
        String publicId = "unknown-article";

        when(articleRepository.findByPublicId(publicId))
                .thenReturn(Optional.empty());

        assertThrows(
                ArticleNotFoundException.class,
                () -> articleService.findByPublicId(publicId)
        );

        verifyNoInteractions(articleMapper);
    }

    @Test
    void create_shouldSaveArticleAndReturnDto() {
        String topicPublicId = "topic-public-id";
        ArticleRequestDto request = new ArticleRequestDto(
                topicPublicId,
                "Test title",
                "Test content"
        );

        Topic topic = new Topic();
        User author = new User();
        Article articleToSave = new Article();
        Article savedArticle = new Article();
        ArticleResponseDto expectedDto = mock(ArticleResponseDto.class);

        when(topicService.existsByPublicId(topicPublicId)).thenReturn(true);
        when(topicService.loadByPublicId(topicPublicId)).thenReturn(topic);
        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleMapper.toEntity(request)).thenReturn(articleToSave);
        when(articleRepository.save(articleToSave)).thenReturn(savedArticle);
        when(articleMapper.toDto(savedArticle)).thenReturn(expectedDto);

        ArticleResponseDto result = articleService.create(request);

        assertSame(expectedDto, result);
        assertSame(author, articleToSave.getAuthor());
        assertSame(topic, articleToSave.getTopic());

        verify(articleRepository).save(articleToSave);
        verify(articleMapper).toDto(savedArticle);
    }

    @Test
    void create_shouldThrowWhenTopicDoesNotExist() {
        String topicPublicId = "unknown-topic";
        ArticleRequestDto request = new ArticleRequestDto(
                topicPublicId,
                "Test title",
                "Test content"
        );

        when(topicService.existsByPublicId(topicPublicId)).thenReturn(false);

        assertThrows(
                TopicNotFoundException.class,
                () -> articleService.create(request)
        );

        verify(topicService, never()).loadByPublicId(anyString());
        verifyNoInteractions(articleRepository);
        verifyNoInteractions(articleMapper);
        verifyNoInteractions(userService);
    }

    @Test
    void loadArticleByPublicId_shouldReturnArticleFromRepository() {
        String publicId = "article-public-id";
        Article expectedArticle = new Article();

        when(articleRepository.getReferenceByPublicId(publicId))
                .thenReturn(expectedArticle);

        Article result = articleService.loadArticleByPublicId(publicId);

        assertSame(expectedArticle, result);
        verify(articleRepository).getReferenceByPublicId(publicId);
    }
}