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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

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

    @Test
    void create_shouldSaveCommentAndReturnResponseDto() {
        String publicId = "article-public-id";
        CommentRequestDto requestDto =
                new CommentRequestDto("This is a test comment");

        User author = new User();
        Article article = new Article();

        Comment commentToSave = new Comment();
        Comment savedComment = new Comment();
        savedComment.setId(1L);

        CommentResponseDto expectedDto =
                new CommentResponseDto(
                        "John_Doe",
                        "This is a test comment",
                        LocalDateTimeTestFactory.generateCreatedAt()
                );

        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleService.loadArticleByPublicId(publicId))
                .thenReturn(article);
        when(commentMapper.toEntity(requestDto)).thenReturn(commentToSave);
        when(commentRepository.save(commentToSave)).thenReturn(savedComment);
        when(commentMapper.toDto(savedComment)).thenReturn(expectedDto);

        CommentResponseDto result = commentService.create(publicId, requestDto);

        assertNotNull(result);
        assertEquals(expectedDto, result);

        assertSame(author, commentToSave.getAuthor());
        assertSame(article, commentToSave.getArticle());

        verify(userService).loadCurrentUserAuthor();
        verify(articleService).loadArticleByPublicId(publicId);
        verify(commentMapper).toEntity(requestDto);
        verify(commentRepository).save(commentToSave);
        verify(commentMapper).toDto(savedComment);
        verifyNoMoreInteractions(
                commentRepository,
                commentMapper,
                userService,
                articleService
        );
    }

    @Test
    void create_shouldPropagateExceptionWhenAuthorCannotBeLoaded() {
        String publicId = "article-public-id";
        CommentRequestDto requestDto =
                new CommentRequestDto("This is a test comment");

        RuntimeException exception =
                new RuntimeException("Author not found");

        when(userService.loadCurrentUserAuthor()).thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> commentService.create(publicId, requestDto)
        );

        assertSame(exception, thrown);

        verify(userService).loadCurrentUserAuthor();
        verifyNoInteractions(
                articleService,
                commentMapper,
                commentRepository
        );
    }

    @Test
    void create_shouldPropagateExceptionWhenArticleCannotBeLoaded() {
        String publicId = "article-public-id";
        CommentRequestDto requestDto =
                new CommentRequestDto("This is a test comment");

        User author = new User();
        RuntimeException exception =
                new RuntimeException("Article not found");

        when(userService.loadCurrentUserAuthor()).thenReturn(author);
        when(articleService.loadArticleByPublicId(publicId))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> commentService.create(publicId, requestDto)
        );

        assertSame(exception, thrown);

        verify(userService).loadCurrentUserAuthor();
        verify(articleService).loadArticleByPublicId(publicId);
        verifyNoInteractions(commentMapper, commentRepository);
    }
}