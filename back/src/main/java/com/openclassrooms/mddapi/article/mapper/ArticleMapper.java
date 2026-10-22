package com.openclassrooms.mddapi.article.mapper;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.comment.mapper.CommentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(
        componentModel = "spring",
        uses = {
                CommentMapper.class,
        }
)
public interface ArticleMapper {

    @Mapping(source = "author.username", target = "username")
    ArticleResponseDto toDto(Article article);

    Article toEntity(ArticleRequestDto articleDto);
}
