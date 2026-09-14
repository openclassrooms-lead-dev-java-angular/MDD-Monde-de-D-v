package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.user.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDetailsMapper {

    @Mapping(source = "email", target = "username")
    UserDetailsImpl toUserDetails(User user);
}
