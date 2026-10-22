package com.openclassrooms.mddapi.comment.service;

import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.entity.Comment;
import com.openclassrooms.mddapi.comment.mapper.CommentMapper;
import com.openclassrooms.mddapi.comment.repository.CommentRepository;
import com.openclassrooms.mddapi.factory.LocalDateTimeTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final String ARTICLE_SLUG = "test-article";
    private static final String CONTENT = "Mon commentaire de test";

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserService userService;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private CommentService commentService;

    private User author;
    private Article article;
    private CommentRequestDto requestDto;
    private Comment comment;
    private Comment savedComment;
    private CommentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setUsername("john_doe");

        article = new Article();
        article.setSlug(ARTICLE_SLUG);

        requestDto = new CommentRequestDto(CONTENT);

        comment = new Comment();
        comment.setContent(CONTENT);

        savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setContent(CONTENT);
        savedComment.setAuthor(author);
        savedComment.setArticle(article);

        responseDto = new CommentResponseDto(
                "john_doe",
                CONTENT,
                LocalDateTimeTestFactory.generateCreatedAt()
        );
    }

    @Test
    void shouldCreateComment() {
        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleService.loadArticleBySlug(ARTICLE_SLUG)).thenReturn(article);
        when(commentMapper.toEntity(requestDto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(savedComment);
        when(commentMapper.toDto(savedComment)).thenReturn(responseDto);

        CommentResponseDto result = commentService.create(ARTICLE_SLUG, requestDto);

        assertThat(result).isSameAs(responseDto);

        verify(userService).loadCurrentUserAuthor();
        verify(articleService).loadArticleBySlug(ARTICLE_SLUG);
        verify(commentMapper).toEntity(requestDto);
        verify(commentRepository).save(comment);
        verify(commentMapper).toDto(savedComment);
    }

    @Test
    void shouldAssociateAuthorAndArticleBeforeSaving() {
        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleService.loadArticleBySlug(ARTICLE_SLUG)).thenReturn(article);
        when(commentMapper.toEntity(requestDto)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentMapper.toDto(savedComment)).thenReturn(responseDto);

        commentService.create(ARTICLE_SLUG, requestDto);

        assertThat(comment.getAuthor()).isSameAs(author);
        assertThat(comment.getArticle()).isSameAs(article);
    }

    @Test
    void shouldSaveMappedComment() {
        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleService.loadArticleBySlug(ARTICLE_SLUG)).thenReturn(article);
        when(commentMapper.toEntity(requestDto)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentMapper.toDto(savedComment)).thenReturn(responseDto);

        commentService.create(ARTICLE_SLUG, requestDto);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository).save(captor.capture());

        Comment capturedComment = captor.getValue();

        assertThat(capturedComment.getContent()).isEqualTo(CONTENT);
        assertThat(capturedComment.getAuthor()).isSameAs(author);
        assertThat(capturedComment.getArticle()).isSameAs(article);
    }

    @Test
    void shouldReturnDtoMappedFromSavedComment() {
        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleService.loadArticleBySlug(ARTICLE_SLUG)).thenReturn(article);
        when(commentMapper.toEntity(requestDto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(savedComment);
        when(commentMapper.toDto(savedComment)).thenReturn(responseDto);

        CommentResponseDto result = commentService.create(ARTICLE_SLUG, requestDto);

        assertThat(result.username()).isEqualTo("john_doe");
        assertThat(result.content()).isEqualTo(CONTENT);
    }
}