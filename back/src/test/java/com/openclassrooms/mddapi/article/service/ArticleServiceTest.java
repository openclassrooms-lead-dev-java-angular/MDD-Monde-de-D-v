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
import com.openclassrooms.mddapi.factory.ArticleTestFactory;
import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.storage.service.StorageService;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.service.TopicService;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private StorageService storageService;

    @Mock
    private TopicService topicService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(articleService, "resourceType", "articles");
    }

    // --------------------------------------------------
    // findAll(Pageable pageable)
    // --------------------------------------------------

    @Test
    void shouldFindAllArticles() {
        Article article = ArticleTestFactory.createArticle(false);

        ArticleResponseDto response = ArticleTestFactory.createArticleResponseDto(false);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Article> articlePage = new PageImpl<>(List.of(article), pageable, 1);

        when(articleRepository.findAll(pageable))
                .thenReturn(articlePage);
        when(articleMapper.toDto(article))
                .thenReturn(response);

        Page<ArticleResponseDto> result = articleService.findAll(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0))
                .isEqualTo(response);

        verify(articleRepository).findAll(pageable);
        verify(articleMapper).toDto(article);
    }

    // --------------------------------------------------
    // findBySlug(final String slug)
    // --------------------------------------------------

    @Test
    void shouldFindArticleBySlug() {
        Article article = ArticleTestFactory.createArticle(false);

        ArticleResponseDto response = ArticleTestFactory.createArticleResponseDto(false);

        when(articleRepository.findBySlug("my-article"))
                .thenReturn(Optional.of(article));
        when(articleMapper.toDto(article))
                .thenReturn(response);

        ArticleResponseDto result = articleService.findBySlug("my-article");

        assertThat(result).isEqualTo(response);

        verify(articleRepository).findBySlug("my-article");
        verify(articleMapper).toDto(article);
    }

    @Test
    void shouldThrowArticleNotFoundExceptionWhenSlugDoesNotExist() {
        when(articleRepository.findBySlug("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.findBySlug("unknown")).isInstanceOf(ArticleNotFoundException.class).hasMessage("Article not found with slug : unknown");

        verify(articleMapper, never()).toDto(any());
    }

    // --------------------------------------------------
    // ArticleResponseDto create(
    //            final ArticleRequestDto articleRequestDto,
    //            final MultipartFile media
    //    ) {
    // --------------------------------------------------

    @Test
    void shouldCreateArticleWithoutMedia() {
        ArticleRequestDto request = ArticleTestFactory.createArticleRequestDto();
        Article article = ArticleTestFactory.createArticle(false);
        Article savedArticle = ArticleTestFactory.createArticle(false);
        ArticleResponseDto response = ArticleTestFactory.createArticleResponseDto(false);

        Topic topic = TopicTestFactory.createTopic();
        User author = UserTestFactory.createUser();

        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(false);
        when(topicService.existsBySlug("java"))
                .thenReturn(true);
        when(topicService.loadBySlug("java"))
                .thenReturn(topic);
        when(userService.loadCurrentUserAuthor())
                .thenReturn(author);
        when(articleMapper.toEntity(request))
                .thenReturn(article);
        when(articleRepository.save(article))
                .thenReturn(savedArticle);
        when(articleMapper.toDto(savedArticle))
                .thenReturn(response);

        ArticleResponseDto result = articleService.create(request, null);

        assertThat(result).isEqualTo(response);
        assertThat(article.getAuthor()).isEqualTo(author);
        assertThat(article.getTopic()).isEqualTo(topic);

        verify(articleRepository).existsBySlug("my-article");
        verify(topicService).existsBySlug("java");
        verify(topicService).loadBySlug("java");
        verify(userService).loadCurrentUserAuthor();
        verify(articleMapper).toEntity(request);
        verify(articleRepository).save(article);
        verify(storageService, never())
                .upload(any(), anyString(), anyString());
    }

    @Test
    void shouldCreateArticleWithMedia() {
        ArticleRequestDto request = ArticleTestFactory.createArticleRequestDto();
        Article article = ArticleTestFactory.createArticle(false);
        Article savedArticle = ArticleTestFactory.createArticle(false);
        ArticleResponseDto response = ArticleTestFactory.createArticleResponseDto(true);

        Topic topic = TopicTestFactory.createTopic();
        User author = UserTestFactory.createUser();
        MultipartFile media = ArticleTestFactory.createMedia();

        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(false);
        when(topicService.existsBySlug("java"))
                .thenReturn(true);
        when(topicService.loadBySlug("java"))
                .thenReturn(topic);
        when(userService.loadCurrentUserAuthor())
                .thenReturn(author);
        when(articleMapper.toEntity(request))
                .thenReturn(article);
        when(storageService.upload(media, "articles", "my-article"))
                .thenReturn("articles/1/image.jpg");
        when(articleRepository.save(article))
                .thenReturn(savedArticle);
        when(articleMapper.toDto(savedArticle))
                .thenReturn(response);

        ArticleResponseDto result = articleService
                .create(request, media);

        assertThat(result).isEqualTo(response);
        assertThat(article.getMedia())
                .isEqualTo("articles/1/image.jpg");

        verify(storageService)
                .upload(
                        media,
                        "articles",
                        "my-article");
        verify(articleRepository).save(article);
    }

    @Test
    void shouldThrowArticleSlugAlreadyExistsWhenCreatingArticle() {
        ArticleRequestDto request = ArticleTestFactory.createArticleRequestDto();

        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(true);

        assertThatThrownBy(() -> articleService.create(request, null))
                .isInstanceOf(ArticleSlugAlreadyExists.class)
                .hasMessage("Slug already exists : my-article");

        verify(topicService, never())
                .existsBySlug(anyString());
        verify(userService, never())
                .loadCurrentUserAuthor();
        verify(articleRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowTopicNotFoundExceptionWhenCreatingArticle() {
        ArticleRequestDto request = ArticleTestFactory.createArticleRequestDto();

        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(false);
        when(topicService.existsBySlug("java"))
                .thenReturn(false);

        assertThatThrownBy(() -> articleService.create(request, null))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found with slug : java");

        verify(topicService, never())
                .loadBySlug(anyString());
        verify(articleRepository, never())
                .save(any());
    }

    // --------------------------------------------------
    // ArticleResponseDto update(
    //            final String slug,
    //            final ArticleUpdateRequestDto articleRequestDto
    //    ) {
    // --------------------------------------------------

    @Test
    void shouldUpdateArticleWithoutMedia() {
        ArticleUpdateRequestDto request = new ArticleUpdateRequestDto(
                "updated-article",
                "java",
                "Updated article",
                "New article content",
                null,
                false );
        Article article = ArticleTestFactory.createArticle(false);
        ArticleResponseDto response = ArticleTestFactory.createUpdatedArticleResponseDto(false);

        when(articleRepository.existsBySlug("updated-article"))
                .thenReturn(false);
        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(true);
        when(topicService.existsBySlug("java"))
                .thenReturn(true);
        when(articleRepository.getReferenceBySlug("my-article"))
                .thenReturn(article);
        when(articleRepository.save(article))
                .thenReturn(article);
        when(articleMapper.toDto(article))
                .thenReturn(response);

        ArticleResponseDto result = articleService
                .update("my-article", request);

        assertThat(result).isEqualTo(response);

        verify(articleMapper)
                .updateEntity(request, article);
        verify(articleRepository)
                .save(article);
        verify(storageService, never())
                .upload(any(), anyString(), anyString());
        verify(storageService, never())
                .delete(anyString());
    }

    @Test
    void shouldUpdateArticleWithMedia() {
        ArticleUpdateRequestDto request = ArticleTestFactory
                .createArticleUpdateRequestDtoWithMedia();
        Article article = ArticleTestFactory.createArticle(true);
        ArticleResponseDto response = ArticleTestFactory
                .createUpdatedArticleResponseDto(true);

        MultipartFile media = request.media();

        when(articleRepository.existsBySlug("updated-article"))
                .thenReturn(false);
        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(true);
        when(topicService.existsBySlug("Updated article"))
                .thenReturn(true);
        when(articleRepository.getReferenceBySlug("my-article"))
                .thenReturn(article);
        when(storageService.upload(media, "articles", "updated-article"))
                .thenReturn("articles/1/new-image.jpg");
        when(articleRepository.save(article))
                .thenReturn(article);
        when(articleMapper.toDto(article))
                .thenReturn(response);

        ArticleResponseDto result = articleService
                .update("my-article", request);

        assertThat(result).isEqualTo(response);
        assertThat(article.getMedia())
                .isEqualTo("articles/1/new-image.jpg");

        verify(storageService)
                .upload(
                        media,
                        "articles",
                        "updated-article");
        verify(articleRepository)
                .save(article);
        verify(storageService)
                .delete("/articles/1/image.jpg");
    }

    @Test
    void shouldThrowArticleSlugAlreadyExistsWhenUpdatingSlug() {
        ArticleUpdateRequestDto request = ArticleTestFactory.createArticleUpdateRequestDto();

        when(articleRepository.existsBySlug("updated-article"))
                .thenReturn(true);

        assertThatThrownBy(() -> articleService.update("my-article", request))
                .isInstanceOf(ArticleSlugAlreadyExists.class);

        verify(articleRepository, never())
                .getReferenceBySlug(anyString());
        verify(articleRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowArticleNotFoundWhenUpdatingUnknownArticle() {
        ArticleUpdateRequestDto request = ArticleTestFactory.createArticleUpdateRequestDto();

        when(articleRepository.existsBySlug("updated-article"))
                .thenReturn(false);
        when(articleRepository.existsBySlug("unknown"))
                .thenReturn(false);

        assertThatThrownBy(() -> articleService.update("unknown", request))
                .isInstanceOf(ArticleNotFoundException.class).hasMessage("Article not found with slug : unknown");

        verify(articleRepository, never())
                .getReferenceBySlug(anyString());
        verify(articleRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowTopicNotFoundWhenUpdatingArticle() {
        ArticleUpdateRequestDto request = new ArticleUpdateRequestDto(
                "updated-article",
                "java",
                "Updated article",
                "New article content",
                null,
                false );

        when(articleRepository.existsBySlug("updated-article"))
                .thenReturn(false);
        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(true);
        when(topicService.existsBySlug("java"))
                .thenReturn(false);

        assertThatThrownBy(() -> articleService.update("my-article", request))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found with slug : java");

        verify(articleRepository, never())
                .getReferenceBySlug(anyString());
        verify(articleRepository, never())
                .save(any());
    }

    // --------------------------------------------------
    // AvailableSlugDto findAvailableSlug(String slug)
    // --------------------------------------------------

    @Test
    void shouldReturnAvailableWhenSlugDoesNotExist() {
        when(articleRepository.existsBySlug("new-article"))
                .thenReturn(false);

        AvailableSlugDto result = articleService.findAvailableSlug("new-article");

        assertThat(result.available()).isTrue();

        verify(articleRepository).existsBySlug("new-article");
    }

    @Test
    void shouldReturnUnavailableWhenSlugAlreadyExists() {
        when(articleRepository.existsBySlug("my-article"))
                .thenReturn(true);

        AvailableSlugDto result = articleService.findAvailableSlug("my-article");

        assertThat(result.available()).isFalse();

        verify(articleRepository).existsBySlug("my-article");
    }
}