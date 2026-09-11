package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.common.enums.Role;
import com.openclassrooms.mddapi.user.User;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;

import java.time.LocalDateTime;

public class UserTestFactory {

    public static User createAdminUser() {
        User user = new User();
        user.setId(1L)
                .setEmail("john@email.com")
                .setUsername("John_Doe")
                .setFirstName("John")
                .setLastName("doe")
                .setPassword("password")
                .setRole(Role.ADMIN)
                .setLastLoginAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));

        return user;
    }

    public static User createUser() {
        User user = new User();
        user.setId(1L)
                .setEmail("jane@email.com")
                .setUsername("Jane_Doe")
                .setFirstName("Jane")
                .setLastName("Doe")
                .setPassword("password")
                .setRole(Role.USER)
                .setLastLoginAt(LocalDateTime.of(2026, 9, 1, 10, 0))
                .setCreatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 7, 1, 10, 0));

        return user;
    }

    public static UserResponseDto createUserResponseDto() {
        return new UserResponseDto(
                1L,
                "jane@email.com",
                "Jane_Doe",
                "Jane",
                "Doe",
                Role.USER,
                LocalDateTime.of(2026, 9, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 7, 1, 10, 0)
        );
    }

    public static UpdateUserDto createUpdateUserDto() {
        return new UpdateUserDto(
                "jane@email.com",
                "Jane Doe",
                "Jane",
                "Doe"
        );
    }
}
