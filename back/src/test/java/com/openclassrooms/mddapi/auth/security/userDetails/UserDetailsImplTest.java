package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    void shouldSetAndGetUserDetailsProperties() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl();

        // When
        userDetails.setId(123L);
        userDetails.setUsername("john.doe@example.com");
        userDetails.setPassword("encoded-password");
        userDetails.setRole(Role.USER);

        // Then
        assertThat(userDetails.getId())
                .isEqualTo(123L);
        assertThat(userDetails.getUsername())
                .isEqualTo("john.doe@example.com");
        assertThat(userDetails.getPassword())
                .isEqualTo("encoded-password");
        assertThat(userDetails.getRole())
                .isEqualTo(Role.USER);
    }

    @Test
    void shouldReturnPassword() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setPassword("encoded-password");

        // When
        String password = userDetails.getPassword();

        // Then
        assertThat(password)
                .isEqualTo("encoded-password");
    }

    @Test
    void shouldReturnUserRoleAsGrantedAuthority() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setRole(Role.USER);

        // When
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Then
        assertThat(authorities)
                .hasSize(1);
        assertThat(authorities.iterator().next().getAuthority())
                .isEqualTo("ROLE_USER");
    }

    @Test
    void shouldReturnAdminRoleAsGrantedAuthority() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setRole(Role.ADMIN);

        // When
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Then
        assertThat(authorities)
                .hasSize(1);
        assertThat(authorities.iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");
    }
}
