package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;

import java.time.LocalDateTime;

public class UserTestFactory {

    public static User createUser() {
        User user = new User();
        user.setId(1L)
                .setEmail("jane@email.com")
                .setUsername("Jane_Doe")
                .setPassword("Password/1234")
                .setCreatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 7, 1, 10, 0));

        return user;
    }

    public static UserResponseDto createUserResponseDto() {
        return new UserResponseDto(
                "jane@email.com",
                "Jane_Doe",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 7, 1, 10, 0)
        );
    }

    public static UpdateUserDto createUpdateUserDto(boolean isMediaUpload) {
        return new UpdateUserDto(
                "jane@email.com",
                "Jane Doe"
        );
    }

    public static RegisterRequestDto createRegisterRequestDto() {
        return new RegisterRequestDto(
                "jane@email.com",
                "Password/1234",
                "Jane Doe"
        );
    }
}
