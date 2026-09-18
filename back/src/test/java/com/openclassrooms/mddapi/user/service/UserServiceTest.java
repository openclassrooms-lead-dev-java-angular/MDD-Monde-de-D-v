package com.openclassrooms.mddapi.user.service;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.common.enums.Role;
import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.exceptions.EmailAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.exceptions.UsernameAlreadyExistsException;
import com.openclassrooms.mddapi.user.mapper.UserMapper;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldSaveAndReturnUser() {
        User user = UserTestFactory.createUser();

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.createUser(user);

        assertThat(result).isSameAs(user);

        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getMeShouldReturnUserResponseDto() {
        Long userId = 1L;

        User user = UserTestFactory.createUser();
        UserResponseDto expectedDto = UserTestFactory.createUserResponseDto();

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userMapper.toDto(user))
                .thenReturn(expectedDto);

        UserResponseDto result = userService.getMe();

        assertThat(result).isSameAs(expectedDto);
        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void getMeShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 999L;

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe())
                .isInstanceOf(UserNotFoundException.class);

        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateUserShouldUpdateAndReturnUserResponseDto() {
        Long userId = 1L;

        User user = UserTestFactory.createUser();
        UpdateUserDto updateUserDto = UserTestFactory.createUpdateUserDto();
        UserResponseDto expectedDto = UserTestFactory.createUserResponseDto();

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);
        when(userRepository.getReferenceById(userId))
                .thenReturn(user);
        when(userRepository.save(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(expectedDto);

        UserResponseDto result = userService.updateUser(updateUserDto);

        assertThat(result).isSameAs(expectedDto);

        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(userRepository).getReferenceById(userId);
        verify(userMapper).updateEntity(updateUserDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void getByEmailShouldReturnUser() {
        String email = "john.doe@example.com";
        User user = UserTestFactory.createUser();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        User result = userService.getByEmail(email);

        assertThat(result).isSameAs(user);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void getByEmailShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService
                .getByEmail(email))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void registerShouldCreateUserWithDefaultRoleAndEncodedPassword() {
        RegisterRequestDto dto = UserTestFactory.createRegisterRequestDto();

        User user = UserTestFactory.createUser();
        String encodedPassword = "encoded-password";

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(false);
        when(passwordEncoder.encode("Password/1234"))
                .thenReturn(encodedPassword);

        userService.register(dto);

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getPassword()).isEqualTo(encodedPassword);

        verify(userMapper).fromRegisterDto(dto);
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).existsByUsername(user.getUsername());
        verify(passwordEncoder).encode("Password/1234");
        verify(userRepository).save(user);
    }

    @Test
    void registerShouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
        RegisterRequestDto dto = UserTestFactory.createRegisterRequestDto();

        User user = UserTestFactory.createUser();

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userMapper).fromRegisterDto(dto);
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowUsernameAlreadyExistsExceptionWhenUsernameAlreadyExists() {
        RegisterRequestDto dto = UserTestFactory.createRegisterRequestDto();

        User user = UserTestFactory.createUser();

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userMapper).fromRegisterDto(dto);
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).existsByUsername(user.getUsername());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowUserAlreadyExistsExceptionWhenDatabaseConstraintIsViolated() {
        RegisterRequestDto dto = UserTestFactory.createRegisterRequestDto();

        User user = UserTestFactory.createUser();

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(false);
        when(passwordEncoder.encode("Password/1234"))
                .thenReturn("encoded-password");

        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class).hasMessage("Username or email already exists");

        verify(userRepository).save(user);
    }

    @Test
    void usernameAvailableShouldReturnTrueWhenUsernameDoesNotExist() {
        String username = "john";

        when(userRepository.existsByUsername(username))
                .thenReturn(false);

        boolean result = userService.usernameAvailable(username);

        assertThat(result).isTrue();

        verify(userRepository).existsByUsername(username);
    }

    @Test
    void usernameAvailableShouldReturnFalseWhenUsernameAlreadyExists() {
        String username = "john";

        when(userRepository.existsByUsername(username))
                .thenReturn(true);

        boolean result = userService.usernameAvailable(username);

        assertThat(result).isFalse();

        verify(userRepository).existsByUsername(username);
    }
}
