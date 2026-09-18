package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    private final UserDetailsMapper userDetailsMapper;

    @Override
    public UserDetailsImpl loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(userDetailsMapper::toUserDetails)
                .orElseThrow(UserNotFoundException::new);
    }

    public UserDetailsImpl loadUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(userDetailsMapper::toUserDetails)
                .orElseThrow(UserNotFoundException::new);
    }
}
