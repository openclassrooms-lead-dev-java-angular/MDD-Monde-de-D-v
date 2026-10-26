package com.openclassrooms.mddapi.auth.security.userDetails;


import com.openclassrooms.mddapi.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserDetailsMapperTest {

    private final UserDetailsMapper mapper =
            Mappers.getMapper(UserDetailsMapper.class);

    @Test
    void toUserDetails_shouldMapEmailToUsername() {
        User user = new User()
                .setEmail("test@example.com");

        UserDetailsImpl result = mapper.toUserDetails(user);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void toUserDetails_shouldReturnNull_whenUserIsNull() {
        UserDetailsImpl result = mapper.toUserDetails(null);

        assertThat(result).isNull();
    }
}