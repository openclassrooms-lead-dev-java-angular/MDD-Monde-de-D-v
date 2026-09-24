package com.openclassrooms.mddapi.auth.security.userDetails;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    void shouldSetAndGetUserDetailsProperties() {
        UserDetailsImpl userDetails = new UserDetailsImpl();

        userDetails.setId(123L);
        userDetails.setUsername("john.doe@example.com");
        userDetails.setPassword("encoded-password");

        assertThat(userDetails.getId())
                .isEqualTo(123L);
        assertThat(userDetails.getUsername())
                .isEqualTo("john.doe@example.com");
        assertThat(userDetails.getPassword())
                .isEqualTo("encoded-password");
    }

    @Test
    void shouldReturnPassword() {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setPassword("encoded-password");

        String password = userDetails.getPassword();

        assertThat(password)
                .isEqualTo("encoded-password");
    }
}
