package com.openclassrooms.mddapi.user.mapper;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(
            target = "avatar",
            expression = "java(entity.getAvatar() != null \n" +
            "? mediaUrl + entity.getAvatar())" +
            ": null)"
    )
    UserResponseDto toDto(
            User entity,
            @Context String mediaUrl
    );

    void updateEntity(
            UpdateUserDto dto,
            @MappingTarget User entity
    );

    User fromRegisterDto(RegisterRequestDto registerDto);
}
