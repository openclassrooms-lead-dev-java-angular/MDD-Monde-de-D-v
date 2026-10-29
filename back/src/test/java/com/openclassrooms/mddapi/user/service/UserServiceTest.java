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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("rawPassword");

        userResponseDto = mock(UserResponseDto.class);
    }

    // -------------------------------------------------------------------------
    // createUser
    // -------------------------------------------------------------------------

    @Test
    void createUserShouldPersistAndReturnUser() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void createUserShouldPropagateRepositoryException() {
        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.createUser(user)
        );

        verify(userRepository).save(user);
    }

    // -------------------------------------------------------------------------
    // getMe
    // -------------------------------------------------------------------------

    @Test
    void getMeShouldReturnMappedUser() {
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getMe();

        assertSame(userResponseDto, result);
        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(user);
    }

    @Test
    void getMeShouldThrowWhenUserDoesNotExist() {
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getMe());

        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getMeShouldPropagatePrincipalException() {
        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenThrow(new RuntimeException("No authenticated user"));

        assertThrows(RuntimeException.class, () -> userService.getMe());

        verifyNoInteractions(userRepository, userMapper);
    }

    // -------------------------------------------------------------------------
    // updateUser
    // -------------------------------------------------------------------------

    @Test
    void updateUserShouldUpdateEncodePasswordAndReturnDto() {
        UpdateUserDto dto = mock(UpdateUserDto.class);

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser(dto);

        assertSame(userResponseDto, result);
        assertEquals("encodedPassword", user.getPassword());

        verify(userMapper).updateEntity(dto, user);
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void updateUserShouldPropagatePrincipalException() {
        UpdateUserDto dto = mock(UpdateUserDto.class);

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenThrow(new RuntimeException("No authenticated user"));

        assertThrows(RuntimeException.class, () -> userService.updateUser(dto));

        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void updateUserShouldPropagateRepositoryException() {
        UpdateUserDto dto = mock(UpdateUserDto.class);

        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.updateUser(dto)
        );

        verify(userMapper).updateEntity(dto, user);
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(user);
        verify(userMapper, never()).toDto(any());
    }

    // -------------------------------------------------------------------------
    // getByEmail
    // -------------------------------------------------------------------------

    @Test
    void getByEmailShouldReturnUser() {
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        User result = userService.getByEmail("john@example.com");

        assertSame(user, result);
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void getByEmailShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getByEmail("unknown@example.com")
        );
    }

    // -------------------------------------------------------------------------
    // register
    // -------------------------------------------------------------------------

    @Test
    void registerShouldMapEncodeAndPersistUser() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        assertDoesNotThrow(() -> userService.register(dto));

        assertEquals("encodedPassword", user.getPassword());

        verify(userMapper).fromRegisterDto(dto);
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).existsByUsername("john");
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(user);
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.register(dto)
        );

        verify(userRepository, never()).existsByUsername(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldThrowWhenUsernameAlreadyExists() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.register(dto)
        );

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldTranslateDataIntegrityViolation() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException("Unique constraint"));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(dto)
        );

        verify(userRepository).save(user);
    }

    @Test
    void registerShouldNotCheckUsernameWhenEmailAlreadyExists() {
        RegisterRequestDto dto = mock(RegisterRequestDto.class);

        when(userMapper.fromRegisterDto(dto)).thenReturn(user);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.register(dto)
        );

        verify(userRepository, never()).existsByUsername(any());
    }

    // -------------------------------------------------------------------------
    // usernameAvailable
    // -------------------------------------------------------------------------

    @Test
    void usernameAvailableShouldReturnTrueWhenUsernameDoesNotExist() {
        when(userRepository.existsByUsername("john")).thenReturn(false);

        boolean result = userService.usernameAvailable("john");

        assertTrue(result);
        verify(userRepository).existsByUsername("john");
    }

    @Test
    void usernameAvailableShouldReturnFalseWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        boolean result = userService.usernameAvailable("john");

        assertFalse(result);
        verify(userRepository).existsByUsername("john");
    }

    // -------------------------------------------------------------------------
    // loadCurrentUserAuthor
    // -------------------------------------------------------------------------

    @Test
    void loadCurrentUserAuthorShouldReturnCurrentUser() {
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(user);

        User result = userService.loadCurrentUserAuthor();

        assertSame(user, result);
        verify(userDetailsServiceImpl).getPrincipalUserId();
        verify(userRepository).getReferenceById(1L);
    }

    @Test
    void loadCurrentUserAuthorShouldPropagatePrincipalException() {
        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenThrow(new RuntimeException("No authenticated user"));

        assertThrows(
                RuntimeException.class,
                () -> userService.loadCurrentUserAuthor()
        );

        verify(userRepository, never()).getReferenceById(anyLong());
    }

    @Test
    void loadCurrentUserAuthorShouldPropagateRepositoryException() {
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L))
                .thenThrow(new UserNotFoundException());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.loadCurrentUserAuthor()
        );
    }
}