package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.auth.exception.UnauthorizedException;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
class UserDetailsServiceImplTest {

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
        userDetailsService = new UserDetailsServiceImpl(
                userRepository,
                userDetailsMapper
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------------------------------
    // loadUserByUsername
    // ---------------------------------

    @Test
    void shouldLoadUserByEmail() {
        String email = "john.doe@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        when(userDetailsMapper.toUserDetails(user))
                .thenReturn(userDetails);

        UserDetailsImpl result =
                userDetailsService.loadUserByUsername(email);

        assertThat(result).isSameAs(userDetails);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userDetailsMapper).toUserDetails(user);
    }

    @Test
    void shouldLoadUserByUsernameWhenEmailIsNotFound() {
        String username = "john.doe";

        when(userRepository.findByEmail(username))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));
        when(userDetailsMapper.toUserDetails(user))
                .thenReturn(userDetails);

        UserDetailsImpl result =
                userDetailsService.loadUserByUsername(username);

        assertThat(result).isSameAs(userDetails);

        verify(userRepository).findByEmail(username);
        verify(userRepository).findByUsername(username);
        verify(userDetailsMapper).toUserDetails(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistByEmailOrUsername() {
        String usernameOrEmail = "unknown@example.com";

        when(userRepository.findByEmail(usernameOrEmail))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername(usernameOrEmail))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername(usernameOrEmail)
        ).isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByEmail(usernameOrEmail);
        verify(userRepository).findByUsername(usernameOrEmail);
        verifyNoInteractions(userDetailsMapper);
    }

    // ---------------------------------
    // loadUserByUserId
    // ---------------------------------

    @Test
    void shouldLoadUserByUserId() {
        Long userId = 123L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userDetailsMapper.toUserDetails(user))
                .thenReturn(userDetails);

        UserDetailsImpl result =
                userDetailsService.loadUserByUserId(userId);

        assertThat(result).isSameAs(userDetails);

        verify(userRepository).findById(userId);
        verify(userDetailsMapper).toUserDetails(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistByUserId() {
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUserId(userId)
        ).isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(userDetailsMapper);
    }

    // ---------------------------------
    // getPrincipalUserId
    // ---------------------------------

    @Test
    void shouldReturnPrincipalUserIdWhenUserIsAuthenticated() {
        Long userId = 42L;
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId.toString());

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        Long result = userDetailsService.getPrincipalUserId();

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenAuthenticationIsNull() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() ->
                userDetailsService.getPrincipalUserId()
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThatThrownBy(() ->
                userDetailsService.getPrincipalUserId()
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void shouldThrowNumberFormatExceptionWhenPrincipalIsNotNumeric() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("invalid-principal");

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThatThrownBy(() ->
                userDetailsService.getPrincipalUserId()
        ).isInstanceOf(NumberFormatException.class);
    }
}