package com.openclassrooms.mddapi.article.service;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.exception.ArticleSlugAlreadyExists;
import com.openclassrooms.mddapi.article.mapper.ArticleMapper;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.factory.ArticleTestFactory;
import com.openclassrooms.mddapi.factory.LocalDateTimeTestFactory;
import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
    void shouldFindFeedArticles() {
        Long userId = 1L;
        Article article = ArticleTestFactory.createArticle();
        ArticleResponseDto response =
                ArticleTestFactory.createArticleResponseDto();
        PageRequest pageable = PageRequest.of(0, 10);

        Page<Article> articlePage = new PageImpl<>(
                List.of(article), pageable, 1
        );

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(articleRepository.findArticlesByUserSubscriptions(userId, pageable))
                .thenReturn(articlePage);
        when(articleMapper.toDto(article)).thenReturn(response);

        Page<ArticleResponseDto> result =
                articleService.findFeedArticles(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent()).containsExactly(response);

        verify(userDetailsService).getPrincipalUserId();
        verify(articleRepository)
                .findArticlesByUserSubscriptions(userId, pageable);
        verify(articleMapper).toDto(article);
    }

    @Test
    void shouldReturnEmptyFeedWhenNoArticlesExist() {
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        when(userDetailsService.getPrincipalUserId()).thenReturn(userId);
        when(articleRepository.findArticlesByUserSubscriptions(userId, pageable))
                .thenReturn(Page.empty(pageable));

        Page<ArticleResponseDto> result =
                articleService.findFeedArticles(pageable);

        assertThat(result).isEmpty();

        verify(userDetailsService).getPrincipalUserId();
        verify(articleRepository)
                .findArticlesByUserSubscriptions(userId, pageable);
        verify(articleMapper, never()).toDto(any(Article.class));
    }

    @Test
    void shouldFindArticleBySlug() {
        Article article = ArticleTestFactory.createArticle();
        ArticleResponseDto response =
                ArticleTestFactory.createArticleResponseDto();

        when(articleRepository.findBySlug("my-article"))
                .thenReturn(Optional.of(article));
        when(articleMapper.toDto(article)).thenReturn(response);

        ArticleResponseDto result = articleService.findBySlug("my-article");

        assertThat(result).isEqualTo(response);

        verify(articleRepository).findBySlug("my-article");
        verify(articleMapper).toDto(article);
    }

    @Test
    void shouldReturnArticleWithCommentsWhenMapperProvidesComments() {
        Article article = ArticleTestFactory.createArticle();
        TopicResponseDto topicResponse = mock(TopicResponseDto.class);
        CommentResponseDto comment1 = mock(CommentResponseDto.class);
        CommentResponseDto comment2 = mock(CommentResponseDto.class);

        ArticleResponseDto response = new ArticleResponseDto(
                "john",
                topicResponse,
                "my-article",
                "My article",
                "Article content",
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt(),
                List.of(comment1, comment2)
        );

        when(articleRepository.findBySlug("my-article"))
                .thenReturn(Optional.of(article));
        when(articleMapper.toDto(article)).thenReturn(response);

        ArticleResponseDto result = articleService.findBySlug("my-article");

        assertThat(result.comments()).containsExactly(comment1, comment2);

        verify(articleRepository).findBySlug("my-article");
        verify(articleMapper).toDto(article);
    }

    @Test
    void shouldReturnArticleWithEmptyCommentsWhenMapperProvidesNoComments() {
        Article article = ArticleTestFactory.createArticle();
        TopicResponseDto topicResponse = mock(TopicResponseDto.class);

        ArticleResponseDto response = new ArticleResponseDto(
                "john",
                topicResponse,
                "my-article",
                "My article",
                "Article content",
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt(),
                List.of()
        );

        when(articleRepository.findBySlug("my-article"))
                .thenReturn(Optional.of(article));
        when(articleMapper.toDto(article)).thenReturn(response);

        ArticleResponseDto result = articleService.findBySlug("my-article");

        assertThat(result.comments()).isEmpty();

        verify(articleRepository).findBySlug("my-article");
        verify(articleMapper).toDto(article);
    }

    @Test
    void shouldThrowArticleNotFoundExceptionWhenSlugDoesNotExist() {
        when(articleRepository.findBySlug("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.findBySlug("unknown"))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessage("Article not found with slug : unknown");

        verify(articleMapper, never()).toDto(any(Article.class));
    }

    @Test
    void shouldCreateArticle() {
        ArticleRequestDto request =
                ArticleTestFactory.createArticleRequestDto();
        Article article = ArticleTestFactory.createArticle();
        Article savedArticle = ArticleTestFactory.createArticle();
        ArticleResponseDto response =
                ArticleTestFactory.createArticleResponseDto();
        Topic topic = TopicTestFactory.createTopic();
        User author = UserTestFactory.createUser();

        when(articleRepository.existsBySlug("my-article")).thenReturn(false);
        when(topicService.existsBySlug("java")).thenReturn(true);
        when(topicService.loadBySlug("java")).thenReturn(topic);
        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleMapper.toEntity(request)).thenReturn(article);
        when(articleRepository.save(article)).thenReturn(savedArticle);
        when(articleMapper.toDto(savedArticle)).thenReturn(response);

        ArticleResponseDto result = articleService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(article.getAuthor()).isEqualTo(author);
        assertThat(article.getTopic()).isEqualTo(topic);

        verify(articleRepository).existsBySlug("my-article");
        verify(topicService).existsBySlug("java");
        verify(topicService).loadBySlug("java");
        verify(userService).loadCurrentUserAuthor();
        verify(articleMapper).toEntity(request);
        verify(articleRepository).save(article);
        verify(articleMapper).toDto(savedArticle);
    }

    @Test
    void shouldThrowArticleSlugAlreadyExistsWhenCreatingArticle() {
        ArticleRequestDto request =
                ArticleTestFactory.createArticleRequestDto();

        when(articleRepository.existsBySlug("my-article")).thenReturn(true);

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(ArticleSlugAlreadyExists.class)
                .hasMessage("Slug already exists : my-article");

        verify(topicService, never()).existsBySlug(anyString());
        verify(topicService, never()).loadBySlug(anyString());
        verify(userService, never()).loadCurrentUserAuthor();
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldThrowTopicNotFoundExceptionWhenCreatingArticle() {
        ArticleRequestDto request =
                ArticleTestFactory.createArticleRequestDto();

        when(articleRepository.existsBySlug("my-article")).thenReturn(false);
        when(topicService.existsBySlug("java")).thenReturn(false);

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found with slug : java");

        verify(topicService, never()).loadBySlug(anyString());
        verify(userService, never()).loadCurrentUserAuthor();
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldReturnAvailableWhenSlugDoesNotExist() {
        when(articleRepository.existsBySlug("new-article")).thenReturn(false);

        AvailableSlugDto result =
                articleService.findAvailableSlug("new-article");

        assertThat(result.available()).isTrue();
        verify(articleRepository).existsBySlug("new-article");
    }

    @Test
    void shouldReturnUnavailableWhenSlugAlreadyExists() {
        when(articleRepository.existsBySlug("my-article")).thenReturn(true);

        AvailableSlugDto result =
                articleService.findAvailableSlug("my-article");

        assertThat(result.available()).isFalse();
        verify(articleRepository).existsBySlug("my-article");
    }

    @Test
    void shouldLoadArticleBySlug() {
        Article article = ArticleTestFactory.createArticle();

        when(articleRepository.getReferenceBySlug("my-article"))
                .thenReturn(article);

        Article result = articleService.loadArticleBySlug("my-article");

        assertThat(result).isEqualTo(article);
        verify(articleRepository).getReferenceBySlug("my-article");
    }
}