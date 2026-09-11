package com.openclassrooms.mddapi.user;

import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User createUser(final User user) {
        User created = userRepository.save(user);

        log.info("User created with username {}: ", user.getUsername());

        return created;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(final Long id) throws NotFoundException {
        User user = userRepository
                .findById(id)
                .orElseThrow(NotFoundException::new);
        log.info("createdAt = {}", user.getCreatedAt());

        return userMapper.toDto(user);
//        return  userRepository
//                .findById(id)
//                .map(userMapper::toDto)
//                .orElseThrow(NotFoundException::new);
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
}
