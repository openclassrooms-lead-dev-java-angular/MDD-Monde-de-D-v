package com.openclassrooms.mddapi.user;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.common.enums.Role;
import com.openclassrooms.mddapi.common.exception.*;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(final User user) {
        User created = userRepository.save(user);

        log.info("User created with username {}: ", user.getUsername());

        return created;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(final Long id) throws NotFoundException {
        return  userRepository
                .findById(id)
                .map(userMapper::toDto)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public UserResponseDto updateUser(final Long id, final UpdateUserDto userDto) {

        User user = userRepository.getReferenceById(id);

        // todo check user == Authentication.getPrincipal

        userMapper.updateEntity(userDto, user);

        User updatedUser = userRepository.save(user);
        log.info("User updated with id {}: ", user.getId());

        return userMapper.toDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public User getByEmail(final String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void register(final RegisterRequestDto dto) {
        User user = userMapper.fromRegisterDto(dto);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Username or email already exists");
        }
    }

    @Transactional(readOnly = true)
    public boolean usernameAvailable(final String username) {
        return !userRepository.existsByUsername(username);
    }
}
