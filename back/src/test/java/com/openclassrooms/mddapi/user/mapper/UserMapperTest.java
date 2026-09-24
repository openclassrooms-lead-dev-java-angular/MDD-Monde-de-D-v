package com.openclassrooms.mddapi.user.mapper;

import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserMapperTest {

    private static final String MEDIA_URL =
            "http://localhost:8080/uploads/";

    private final UserMapper userMapper =
            Mappers.getMapper(UserMapper.class);

    @Test
    void toDtoShouldMapUserEntityToUserResponseDto() {
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 10, 10, 0);
        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 2, 10, 10, 0);
        User user =
                UserTestFactory.createUser();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setUsername("john");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAvatar("avatars/john/image.jpg");
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        UserResponseDto result =
                userMapper.toDto(user, MEDIA_URL);

        assertThat(result).isNotNull();
        assertThat(result.id())
                .isEqualTo(1L);
        assertThat(result.email())
                .isEqualTo("john.doe@example.com");
        assertThat(result.username())
                .isEqualTo("john");
        assertThat(result.firstName())
                .isEqualTo("John");
        assertThat(result.lastName())
                .isEqualTo("Doe");
        assertThat(result.avatar())
                .isEqualTo(
                        "http://localhost:8080/uploads/avatars/john/image.jpg"
                );
        assertThat(result.createdAt())
                .isEqualTo(createdAt);
        assertThat(result.updatedAt())
                .isEqualTo(updatedAt);
    }

    @Test
    void updateEntityShouldUpdateEditableUserFields() {
        User user =
                UserTestFactory.createUser();
        user.setId(1L);
        user.setEmail("old@example.com");
        user.setUsername("oldUsername");
        user.setFirstName("Old");
        user.setLastName("Name");
        UpdateUserDto dto = new UpdateUserDto(
                "new@example.com",
                "newUsername",
                "New",
                "User",
                false
        );

        userMapper.updateEntity(dto, user);

        assertThat(user.getEmail())
                .isEqualTo("new@example.com");
        assertThat(user.getUsername())
                .isEqualTo("newUsername");
        assertThat(user.getFirstName())
                .isEqualTo("New");
        assertThat(user.getLastName())
                .isEqualTo("User");
    }

    @Test
    void updateEntityShouldNotModifyTechnicalFields() {
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 10, 10, 0);
        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 2, 10, 10, 0);
        User user =
                UserTestFactory.createUser();
        user.setId(1L);
        user.setEmail("old@example.com");
        user.setUsername("oldUsername");
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        UpdateUserDto dto = new UpdateUserDto(
                "new@example.com",
                "newUsername",
                "New",
                "User",
                false
        );

        userMapper.updateEntity(dto, user);

        assertThat(user.getId())
                .isEqualTo(1L);
        assertThat(user.getCreatedAt())
                .isEqualTo(createdAt);
        assertThat(user.getUpdatedAt())
                .isEqualTo(updatedAt);
    }
}
