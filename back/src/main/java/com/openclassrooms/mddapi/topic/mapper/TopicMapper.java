package com.openclassrooms.mddapi.topic.mapper;

import com.openclassrooms.mddapi.topic.dto.TopicRequestDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface TopicMapper {

    @Mapping(target = "subscribed", source = "subscribed")
    TopicResponseDto toDto(Topic topic, Boolean subscribed);

    Topic toEntity(TopicRequestDto topicRequestDto);

    void updateEntity(
            @MappingTarget Topic topic,
            TopicRequestDto topicRequestDto
    );
}
