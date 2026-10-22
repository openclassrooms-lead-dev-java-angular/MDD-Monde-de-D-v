package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserDetailsMapperTest {

    private final UserDetailsMapper mapper = Mappers.getMapper(UserDetailsMapper.class);

    @Test
    void shouldMapUserToUserDetails() {
        
        User user = UserTestFactory.createUser();

        UserDetailsImpl result = mapper.toUserDetails(user);

        assertThat(result).isNotNull();
        assertThat(result.getId())
                .isEqualTo(1L);
        assertThat(result.getUsername())
                .isEqualTo("jane@email.com");
        assertEquals("Password/1234", result.getPassword());
    }

    @Test
    void shouldReturnNullWhenUserIsNull() {
        
        User user = null;

        UserDetailsImpl result = mapper.toUserDetails(user);

        assertThat(result).isNull();
    }
}
