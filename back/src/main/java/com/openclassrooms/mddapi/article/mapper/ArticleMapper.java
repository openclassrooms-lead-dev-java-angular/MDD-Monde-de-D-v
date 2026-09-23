package com.openclassrooms.mddapi.article.mapper;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.ArticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.entity.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(source = "author.username", target = "username")
    ArticleResponseDto toDto(Article article);

    @Mapping(target = "media", ignore = true)
    Article toEntity(ArticleRequestDto articleDto);

    @Mapping(target = "media", ignore = true)
    void updateEntity(
            ArticleUpdateRequestDto articleDto,
            @MappingTarget Article article
    );
}
