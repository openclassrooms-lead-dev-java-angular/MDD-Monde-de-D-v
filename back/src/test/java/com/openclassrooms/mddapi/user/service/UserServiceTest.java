package com.openclassrooms.mddapi.user.service;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.exceptions.EmailAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.exceptions.UsernameAlreadyExistsException;
import com.openclassrooms.mddapi.user.mapper.UserMapper;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String MEDIA_URL = "http://localhost:8080/uploads";
    private static final String RESOURCE_TYPE = "avatars";

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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "mediaUrl", MEDIA_URL);
        ReflectionTestUtils.setField(userService, "resourceType", RESOURCE_TYPE);
    }

    @Test
    void createUserShouldSaveAndReturnUser() {
        User user = mock(User.class);

        when(user.getUsername()).thenReturn("testuser");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void getMeShouldReturnUserResponseDto() {
        Long userId = 1L;
        User user = mock(User.class);
        UserResponseDto expectedDto = mock(UserResponseDto.class);

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserResponseDto result = userService.getMe();

        assertSame(expectedDto, result);
        verify(userMapper).toDto(user);
    }

    @Test
    void getMeShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 1L;

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getMe()
        );

        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void updateUserShouldUpdateAndReturnUserResponseDtoWithMedia() {
        Long userId = 1L;
        String oldFilename = "old-avatar.png";
        String newFilename = "new-avatar.png";

        User user = mock(User.class);
        UpdateUserDto userDto = mock(UpdateUserDto.class);
        UserResponseDto expectedDto = mock(UserResponseDto.class);
        MultipartFile media = new MockMultipartFile(
                "media",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(userDto.username()).thenReturn("testuser");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserResponseDto result = userService.updateUser(userDto);

        assertSame(expectedDto, result);

        verify(userMapper).updateEntity(userDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void updateUserShouldUpdateWithoutMedia() {
        Long userId = 1L;

        User user = mock(User.class);
        UpdateUserDto userDto = mock(UpdateUserDto.class);
        UserResponseDto expectedDto = mock(UserResponseDto.class);

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserResponseDto result = userService.updateUser(userDto);

        assertSame(expectedDto, result);

        verify(userMapper).updateEntity(userDto, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldNotUploadMediaWhenUpdatedMediaIsFalse() {
        Long userId = 1L;

        User user = mock(User.class);
        UpdateUserDto userDto = mock(UpdateUserDto.class);
        UserResponseDto expectedDto = mock(UserResponseDto.class);
        MultipartFile media = new MockMultipartFile(
                "media",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserResponseDto result = userService.updateUser(userDto);

        assertSame(expectedDto, result);

        verify(userRepository).save(user);
    }

    @Test
    void getByEmailShouldReturnUser() {
        String email = "test@example.com";
        User user = mock(User.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getByEmail(email);

        assertSame(user, result);
    }

    @Test
    void getByEmailShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getByEmail(email)
        );
    }

    @Test
    void registerShouldCreateUserWithEncodedPassword() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);
        User user = mock(User.class);
        String rawPassword = "password123";
        String encodedPassword = "encoded-password";

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getUsername()).thenReturn("testuser");
        when(user.getPassword()).thenReturn(rawPassword);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        userService.register(dto);

        verify(user).setPassword(encodedPassword);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(user);
    }

    @Test
    void registerShouldThrowEmailAlreadyExistsException() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);
        User user = mock(User.class);

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(user.getEmail()).thenReturn("test@example.com");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.register(dto)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerShouldThrowUsernameAlreadyExistsException() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);
        User user = mock(User.class);

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getUsername()).thenReturn("testuser");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.register(dto)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerShouldThrowUserAlreadyExistsExceptionWhenDatabaseConstraintIsViolated() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);
        User user = mock(User.class);
        String rawPassword = "password123";

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getUsername()).thenReturn("testuser");
        when(user.getPassword()).thenReturn(rawPassword);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn("encoded-password");
        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException("Duplicate user"));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(dto)
        );

        verify(userRepository).save(user);
    }

    @Test
    void usernameAvailableShouldReturnTrueWhenUsernameDoesNotExist() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        assertTrue(userService.usernameAvailable("testuser"));
    }

    @Test
    void usernameAvailableShouldReturnFalseWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertFalse(userService.usernameAvailable("testuser"));
    }

    @Test
    void loadCurrentUserAuthorShouldReturnCurrentUser() {
        Long userId = 1L;
        User user = mock(User.class);

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        User result = userService.loadCurrentUserAuthor();

        assertSame(user, result);
        verify(userRepository).getReferenceById(userId);
    }
}

