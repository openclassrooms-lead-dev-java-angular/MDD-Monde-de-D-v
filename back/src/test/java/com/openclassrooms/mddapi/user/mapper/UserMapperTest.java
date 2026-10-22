package com.openclassrooms.mddapi.user.mapper;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void shouldMapUserToDto() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 16, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 16, 11, 0);

        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setUsername("John_Doe");
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        UserResponseDto result = userMapper.toDto(user);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.email());
        assertEquals("John_Doe", result.username());
        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());
    }

    @Test
    void shouldUpdateEntityFromDto() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 16, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 16, 11, 0);

        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setUsername("John_Doe");
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        UpdateUserDto dto = new UpdateUserDto(
                "jane.doe@example.com",
                "Jane_Doe"
        );

        userMapper.updateEntity(dto, user);

        assertEquals("jane.doe@example.com", user.getEmail());
        assertEquals("Jane_Doe", user.getUsername());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
    }

    @Test
    void shouldMapRegisterDtoToUser() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "john.doe@example.com",
                "Password/123",
                "John_Doe"
        );

        User result = userMapper.fromRegisterDto(dto);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("John_Doe", result.getUsername());
        assertEquals("Password/123", result.getPassword());
    }

    @Test
    void shouldReturnNullWhenUserToDtoIsNull() {
        assertNull(userMapper.toDto(null));
    }

    @Test
    void shouldReturnNullWhenRegisterDtoIsNull() {
        assertNull(userMapper.fromRegisterDto(null));
    }
}