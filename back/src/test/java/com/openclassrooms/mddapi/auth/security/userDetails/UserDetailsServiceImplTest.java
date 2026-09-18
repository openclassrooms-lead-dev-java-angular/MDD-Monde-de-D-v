package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.auth.exception.UnauthorizedException;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailsMapper userDetailsMapper;

    @Mock
    private User user;

    @Mock
    private UserDetailsImpl userDetails;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository, userDetailsMapper);
    }


    @Test
    void shouldLoadUserByUsername() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userDetailsMapper.toUserDetails(user)).thenReturn(userDetails);

        // When
        UserDetailsImpl result = userDetailsService.loadUserByUsername(email);

        // Then
        assertThat(result).isSameAs(userDetails);
        verify(userRepository).findByEmail(email);
        verify(userDetailsMapper).toUserDetails(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistByUsername() {
        // Given
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        // When / Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email)).isInstanceOf(UserNotFoundException.class);
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(userDetailsMapper);
    }

    @Test
    void shouldLoadUserByUserId() {
        // Given
        Long userId = 123L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userDetailsMapper.toUserDetails(user)).thenReturn(userDetails);

        // When
        UserDetailsImpl result = userDetailsService.loadUserByUserId(userId);

        // Then
        assertThat(result).isSameAs(userDetails);
        verify(userRepository).findById(userId);
        verify(userDetailsMapper).toUserDetails(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistByUserId() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // When / Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUserId(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(userDetailsMapper);
    }

    // getPrincipalUserId

    @Test
    void shouldReturnPrincipalUserIdWhenUserIsAuthenticated() {
        Long userId = 42L;
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId(userId);

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(true);
        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long result = userDetailsService.getPrincipalUserId();
        assertThat(result).isEqualTo(userId);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenAuthenticationIsNull() {
        SecurityContextHolder.clearContext();

        Assertions.assertThatThrownBy(() -> userDetailsService.getPrincipalUserId())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Assertions.assertThatThrownBy(() -> userDetailsService.getPrincipalUserId())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenPrincipalIsInvalid() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(true);
        when(authentication.getPrincipal())
                .thenReturn("invalid-principal");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Assertions.assertThatThrownBy(() -> userDetailsService.getPrincipalUserId())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid authenticated user");
    }
}
