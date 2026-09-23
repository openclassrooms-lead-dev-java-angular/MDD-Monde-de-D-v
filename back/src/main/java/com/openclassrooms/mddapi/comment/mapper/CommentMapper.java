package com.openclassrooms.mddapi.comment.mapper;

import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "author.username", target = "username")
    CommentResponseDto toDto(Comment comment);

    Comment toEntity(CommentRequestDto commentDto);
}
