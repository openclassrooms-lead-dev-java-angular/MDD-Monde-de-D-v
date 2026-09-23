package com.openclassrooms.mddapi.comment.mapper;

import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.entity.Comment;
import com.openclassrooms.mddapi.factory.CommentTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CommentMapperTest {

    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void shouldMapCommentToDto() {
        Comment comment = CommentTestFactory.createComment();

        CommentResponseDto result = commentMapper.toDto(comment);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("Jane_Doe");
        assertThat(result.content()).isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        assertThat(result.createdAt()).isEqualTo(comment.getCreatedAt());
    }

    @Test
    void shouldMapCommentRequestDtoToEntity() {
        CommentRequestDto request = CommentTestFactory.createCommentRequestDto();

        Comment result = commentMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getContent())
                .isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
    }

    @Test
    void shouldReturnNullWhenMappingNullComment() {
        CommentResponseDto result = commentMapper.toDto(null);
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenMappingNullRequest() {
        Comment result = commentMapper.toEntity(null);
        assertThat(result).isNull();
    }
}
