package com.openclassrooms.mddapi.user.service;

import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.auth.service.AuthService;
import com.openclassrooms.mddapi.common.enums.Role;
import com.openclassrooms.mddapi.user.exceptions.EmailAlreadyExistsException;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.exceptions.UsernameAlreadyExistsException;
import com.openclassrooms.mddapi.user.mapper.UserMapper;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for user management operations.
 *
 * <p>Handles user creation, retrieval, update and registration,
 * including password encoding and uniqueness checks.</p>
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * Persists a new user.
     *
     * @param user the user to create
     * @return the persisted user
     */
    @Transactional
    public User createUser(final User user) {
        User created = userRepository.save(user);

        log.info("User created with username {}: ", user.getUsername());

        return created;
    }

    /**
     * Retrieves a user by its identifier and maps it to a response DTO.
     *
     * @return the user response DTO
     * @throws UserNotFoundException if no user exists with the given identifier
     */
    @Transactional(readOnly = true)
    public UserResponseDto getMe() {
        Long userId = userDetailsServiceImpl.getPrincipalUserId();

        return userRepository
                .findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Updates an existing user with the provided information.
     *
     * <p>The current implementation is intended to restrict updates
     * to the authenticated user.</p>
     *
     * @param userDto the user data to apply
     * @return the updated user response DTO
     * @throws UserNotFoundException if no user exists with the given identifier
     */
    @Transactional
    public UserResponseDto updateUser(final UpdateUserDto userDto) {
        Long userId = userDetailsServiceImpl.getPrincipalUserId();

        User user = userRepository.getReferenceById(userId);

        userMapper.updateEntity(userDto, user);

        User updatedUser = userRepository.save(user);
        log.info("User updated with id {}: ", user.getId());

        return userMapper.toDto(updatedUser);
    }

    /**
     * Retrieves a user by email address.
     *
     * @param email the user's email address
     * @return the user associated with the email address
     * @throws UserNotFoundException if no user exists with the given email
     */
    @Transactional(readOnly = true)
    public User getByEmail(final String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Registers a new user.
     *
     * <p>Checks email and username uniqueness, assigns the default
     * { @link Role#USER } role and encodes the user's password before
     * persisting the user.</p>
     *
     * @param dto the registration data
     * @throws EmailAlreadyExistsException if the email is already registered
     * @throws UsernameAlreadyExistsException if the username is already registered
     * @throws UserAlreadyExistsException if a database constraint violation
     * occurs during registration
     */
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

    /**
     * Checks whether a username is available for registration.
     *
     * @param username the username to check
     * @return {@code true} if the username is available, {@code false} otherwise
     */
    @Transactional(readOnly = true)
    public boolean usernameAvailable(final String username) {

        return !userRepository.existsByUsername(username);
    }
}
