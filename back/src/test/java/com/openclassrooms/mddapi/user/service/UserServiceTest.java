package com.openclassrooms.mddapi.user.service;

import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.mapper.UserMapper;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;

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
    void getByIdShouldReturnUserResponseDto() throws ChangeSetPersister.NotFoundException {
        Long id = 1L;

        User user = UserTestFactory.createUser();
        UserResponseDto expectedDto = UserTestFactory.createUserResponseDto();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user))
                .thenReturn(expectedDto);

        UserResponseDto result = userService.getMe();

        assertThat(result).isSameAs(expectedDto);

        verify(userRepository).findById(id);
        verify(userMapper).toDto(user);
    }

    @Test
    void getByIdShouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        Long id = 999L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe())
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(id);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateUserShouldUpdateAndReturnUserResponseDto() {
        Long id = 1L;

        User user = UserTestFactory.createUser();
        UpdateUserDto updateUserDto =
                UserTestFactory.createUpdateUserDto();

        UserResponseDto expectedDto =
                UserTestFactory.createUserResponseDto();

        when(userRepository.getReferenceById(id))
                .thenReturn(user);
        when(userRepository.save(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(expectedDto);

        UserResponseDto result =
                userService.updateUser(updateUserDto);

        assertThat(result).isSameAs(expectedDto);

        verify(userRepository).getReferenceById(id);
        verify(userMapper).updateEntity(updateUserDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }
}
