package com.openclassrooms.mddapi.article.mapper;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(source = "author.username", target = "username")
    @Mapping(target = "media", expression = "java(buildMediaUrl(article.getMedia(), mediaUrl))")
    ArticleResponseDto toDto(
            Article article,
            @Context String mediaUrl
    );

    @Mapping(target = "media", ignore = true)
    Article toEntity(ArticleRequestDto articleDto);

    default String buildMediaUrl(String media, String mediaUrl) {
        if (media == null) {
            return null;
        }
        return mediaUrl + media;
    }
}
