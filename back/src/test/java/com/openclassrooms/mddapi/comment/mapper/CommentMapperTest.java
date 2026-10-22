package com.openclassrooms.mddapi.comment.mapper;

import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.entity.Comment;
import com.openclassrooms.mddapi.factory.CommentTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() {
        commentMapper = Mappers.getMapper(CommentMapper.class);
    }

    @Test
    void shouldMapCommentToDto() {
        User author = new User();
        author.setUsername("john_doe");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Mon commentaire");
        comment.setAuthor(author);

        CommentResponseDto result = commentMapper.toDto(comment);

        assertNotNull(result);
        assertEquals("Mon commentaire", result.content());
        assertEquals("john_doe", result.username());
    }

    @Test
    void shouldMapCommentRequestDtoToEntity() {
        CommentRequestDto requestDto =
                new CommentRequestDto("Mon commentaire");

        Comment result = commentMapper.toEntity(requestDto);

        assertNotNull(result);
        assertEquals("Mon commentaire", result.getContent());
    }

    @Test
    void shouldReturnNullWhenCommentIsNull() {
        CommentResponseDto result = commentMapper.toDto(null);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenCommentRequestDtoIsNull() {
        Comment result = commentMapper.toEntity(null);

        assertNull(result);
    }
}