package com.openclassrooms.mddapi.user.service;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.factory.MediaTestFactory;
import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.storage.service.StorageService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String MEDIA_URL =
            "http://localhost:8080/uploads";

    private static final String RESOURCE_TYPE =
            "avatars";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                userService,
                "mediaUrl",
                MEDIA_URL
        );

        ReflectionTestUtils.setField(
                userService,
                "resourceType",
                RESOURCE_TYPE
        );
    }

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
        verifyNoInteractions(storageService);
    }

    @Test
    void getMeShouldReturnUserResponseDto() {
        Long userId = 1L;

        User user = UserTestFactory.createUser();
        UserResponseDto expectedDto =
                UserTestFactory.createUserResponseDto();

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user, MEDIA_URL))
                .thenReturn(expectedDto);

        UserResponseDto result =
                userService.getMe();

        assertThat(result)
                .isSameAs(expectedDto);

        verify(userDetailsServiceImpl)
                .getPrincipalUserId();

        verify(userRepository)
                .findById(userId);

        verify(userMapper)
                .toDto(user, MEDIA_URL);

        verifyNoInteractions(storageService);
    }

    @Test
    void getMeShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 999L;

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getMe()
        )
                .isInstanceOf(UserNotFoundException.class);

        verify(userDetailsServiceImpl)
                .getPrincipalUserId();

        verify(userRepository)
                .findById(userId);

        verifyNoInteractions(userMapper);
        verifyNoInteractions(storageService);
    }

    @Test
    void updateUserShouldUpdateAndReturnUserResponseDtoWithMedia() {
        Long userId = 1L;

        User user = UserTestFactory.createUser();
        String oldAvatar = user.getAvatar();

        UpdateUserDto updateUserDto =
                UserTestFactory.createUpdateUserDto(true);

        MockMultipartFile media =
                MediaTestFactory.createMedia();

        UserResponseDto expectedDto =
                UserTestFactory.createUserResponseDto();

        String newFilename =
                "avatars/john/image.jpg";

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(storageService.upload(
                media,
                RESOURCE_TYPE,
                updateUserDto.username()
        ))
                .thenReturn(newFilename);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toDto(user, MEDIA_URL))
                .thenReturn(expectedDto);

        UserResponseDto result =
                userService.updateUser(
                        updateUserDto,
                        media
                );

        assertThat(result)
                .isSameAs(expectedDto);

        verify(userDetailsServiceImpl)
                .getPrincipalUserId();

        verify(userRepository)
                .getReferenceById(userId);

        verify(userMapper)
                .updateEntity(updateUserDto, user);

        verify(storageService)
                .upload(
                        media,
                        RESOURCE_TYPE,
                        updateUserDto.username()
                );

        verify(userRepository)
                .save(user);

        verify(storageService)
                .delete(oldAvatar);

        verify(userMapper)
                .toDto(user, MEDIA_URL);
    }

    @Test
    void updateUserShouldUpdateWithoutMedia() {
        Long userId = 1L;

        User user = UserTestFactory.createUser();

        UpdateUserDto updateUserDto =
                UserTestFactory.createUpdateUserDto(false);

        UserResponseDto expectedDto =
                UserTestFactory.createUserResponseDto();

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toDto(user, MEDIA_URL))
                .thenReturn(expectedDto);

        UserResponseDto result =
                userService.updateUser(
                        updateUserDto,
                        null
                );

        assertThat(result)
                .isSameAs(expectedDto);

        verify(userDetailsServiceImpl)
                .getPrincipalUserId();

        verify(userRepository)
                .getReferenceById(userId);

        verify(userMapper)
                .updateEntity(updateUserDto, user);

        verify(userRepository)
                .save(user);

        verify(storageService, never())
                .upload(
                        any(),
                        anyString(),
                        anyString()
                );

        verify(storageService, never())
                .delete(anyString());

        verify(userMapper)
                .toDto(user, MEDIA_URL);
    }

    @Test
    void updateUserShouldNotUploadMediaWhenUpdatedMediaIsFalse() {
        Long userId = 1L;

        User user = UserTestFactory.createUser();

        UpdateUserDto updateUserDto = new UpdateUserDto(
                "john.doe@example.com",
                "john",
                "John",
                "Doe",
                false
        );

        MockMultipartFile media =
                MediaTestFactory.createMedia();

        UserResponseDto expectedDto =
                UserTestFactory.createUserResponseDto();

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toDto(user, MEDIA_URL))
                .thenReturn(expectedDto);

        UserResponseDto result =
                userService.updateUser(
                        updateUserDto,
                        media
                );

        assertThat(result)
                .isSameAs(expectedDto);

        verify(userMapper)
                .updateEntity(updateUserDto, user);

        verify(userRepository)
                .save(user);

        verify(storageService, never())
                .upload(
                        any(),
                        anyString(),
                        anyString()
                );

        verify(storageService, never())
                .delete(anyString());

        verify(userMapper)
                .toDto(user, MEDIA_URL);
    }

    @Test
    void getByEmailShouldReturnUser() {
        String email = "john.doe@example.com";

        User user =
                UserTestFactory.createUser();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        User result =
                userService.getByEmail(email);

        assertThat(result)
                .isSameAs(user);

        verify(userRepository)
                .findByEmail(email);
    }

    @Test
    void getByEmailShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getByEmail(email)
        )
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository)
                .findByEmail(email);
    }

    @Test
    void registerShouldCreateUserWithEncodedPasswordAndUploadMedia() {
        RegisterRequestDto dto =
                UserTestFactory.createRegisterRequestDto();

        User user =
                UserTestFactory.createUser();

        MockMultipartFile media =
                MediaTestFactory.createMedia();

        String encodedPassword =
                "encoded-password";

        String filename =
                "avatars/john/image.jpg";

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(false);

        when(passwordEncoder.encode("Password/1234"))
                .thenReturn(encodedPassword);

        when(storageService.upload(
                media,
                RESOURCE_TYPE,
                dto.username()
        ))
                .thenReturn(filename);

        userService.register(dto, media);

        assertThat(user.getPassword())
                .isEqualTo(encodedPassword);

        assertThat(user.getAvatar())
                .isEqualTo(filename);

        verify(userMapper)
                .fromRegisterDto(dto);

        verify(userRepository)
                .existsByEmail(user.getEmail());

        verify(userRepository)
                .existsByUsername(user.getUsername());

        verify(passwordEncoder)
                .encode("Password/1234");

        verify(storageService)
                .upload(
                        media,
                        RESOURCE_TYPE,
                        dto.username()
                );

        verify(userRepository)
                .save(user);
    }

    @Test
    void registerShouldCreateUserWithoutMedia() {
        RegisterRequestDto dto =
                UserTestFactory.createRegisterRequestDto();

        User user =
                UserTestFactory.createUser();

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(false);

        when(passwordEncoder.encode("Password/1234"))
                .thenReturn("encoded-password");

        userService.register(dto, null);

        assertThat(user.getPassword())
                .isEqualTo("encoded-password");

        verify(userMapper)
                .fromRegisterDto(dto);

        verify(passwordEncoder)
                .encode("Password/1234");

        verify(storageService, never())
                .upload(
                        any(),
                        anyString(),
                        anyString()
                );

        verify(userRepository)
                .save(user);
    }

    @Test
    void registerShouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
        RegisterRequestDto dto =
                UserTestFactory.createRegisterRequestDto();

        User user =
                UserTestFactory.createUser();

        MockMultipartFile media =
                MediaTestFactory.createMedia();

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.register(dto, media)
        )
                .isInstanceOf(
                        EmailAlreadyExistsException.class
                );

        verify(userMapper)
                .fromRegisterDto(dto);

        verify(userRepository)
                .existsByEmail(user.getEmail());

        verify(userRepository, never())
                .existsByUsername(anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(storageService, never())
                .upload(
                        any(),
                        anyString(),
                        anyString()
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void registerShouldThrowUsernameAlreadyExistsExceptionWhenUsernameAlreadyExists() {
        RegisterRequestDto dto =
                UserTestFactory.createRegisterRequestDto();

        User user =
                UserTestFactory.createUser();

        MockMultipartFile media =
                MediaTestFactory.createMedia();

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.register(dto, media)
        )
                .isInstanceOf(
                        UsernameAlreadyExistsException.class
                );

        verify(userMapper)
                .fromRegisterDto(dto);

        verify(userRepository)
                .existsByEmail(user.getEmail());

        verify(userRepository)
                .existsByUsername(user.getUsername());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(storageService, never())
                .upload(
                        any(),
                        anyString(),
                        anyString()
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void registerShouldThrowUserAlreadyExistsExceptionWhenDatabaseConstraintIsViolated() {
        RegisterRequestDto dto =
                UserTestFactory.createRegisterRequestDto();

        User user =
                UserTestFactory.createUser();

        when(userMapper.fromRegisterDto(dto))
                .thenReturn(user);

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(false);

        when(passwordEncoder.encode("Password/1234"))
                .thenReturn("encoded-password");

        when(userRepository.save(user))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "Duplicate key"
                        )
                );

        assertThatThrownBy(() ->
                userService.register(dto, null)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage(
                        "Username or email already exists"
                );

        verify(userRepository)
                .save(user);

        verify(storageService, never())
                .upload(
                        any(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void usernameAvailableShouldReturnTrueWhenUsernameDoesNotExist() {
        String username = "john";

        when(userRepository.existsByUsername(username))
                .thenReturn(false);

        boolean result =
                userService.usernameAvailable(username);

        assertThat(result)
                .isTrue();

        verify(userRepository)
                .existsByUsername(username);
    }

    @Test
    void usernameAvailableShouldReturnFalseWhenUsernameAlreadyExists() {
        String username = "john";

        when(userRepository.existsByUsername(username))
                .thenReturn(true);

        boolean result =
                userService.usernameAvailable(username);

        assertThat(result)
                .isFalse();

        verify(userRepository)
                .existsByUsername(username);
    }

    @Test
    void loadCurrentUserAuthorShouldReturnCurrentUser() {
        Long userId = 1L;

        User user =
                UserTestFactory.createUser();

        when(userDetailsServiceImpl.getPrincipalUserId())
                .thenReturn(userId);

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        User result =
                userService.loadCurrentUserAuthor();

        assertThat(result)
                .isSameAs(user);

        verify(userDetailsServiceImpl)
                .getPrincipalUserId();

        verify(userRepository)
                .getReferenceById(userId);
    }
}

