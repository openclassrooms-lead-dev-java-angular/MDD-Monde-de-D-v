package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.common.enums.Role;
import com.openclassrooms.mddapi.factory.UserTestFactory;

import com.openclassrooms.mddapi.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDetailsMapperTest {

    private final UserDetailsMapper mapper = Mappers.getMapper(UserDetailsMapper.class);

    @Test
    void shouldMapUserToUserDetails() {
        // Given
        User user = UserTestFactory.createUser();

        // When
        UserDetailsImpl result = mapper.toUserDetails(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId())
                .isEqualTo(1L);
        assertThat(result.getUsername())
                .isEqualTo("jane@email.com");
        assertThat(result.getPassword())
                .isEqualTo("password");
        assertThat(result.getRole())
                .isEqualTo(Role.USER);
    }

    @Test
    void shouldReturnNullWhenUserIsNull() {
        // Given
        User user = null;

        // When
        UserDetailsImpl result = mapper.toUserDetails(user);

        // Then
        assertThat(result).isNull();
    }
}
