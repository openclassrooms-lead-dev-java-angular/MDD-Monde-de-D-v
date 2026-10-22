package com.openclassrooms.mddapi.topic.mapper;

import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface TopicMapper {

    @Mapping(target = "subscribed", source = "subscribed")
    TopicResponseDto toDto(Topic topic, Boolean subscribed);

}
