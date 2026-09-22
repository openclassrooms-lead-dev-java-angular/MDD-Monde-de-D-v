package com.openclassrooms.mddapi.auth.security.userDetails;

import com.openclassrooms.mddapi.auth.exception.UnauthorizedException;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for loading authenticated user details for Spring Security.
 *
 * <p>Users can be loaded either by their email address or by their identifier.
 * This service also provides access to the identifier of the currently
 * authenticated user.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    private final UserDetailsMapper userDetailsMapper;

    /**
     * Loads a user by their email address.
     *
     * <p>This method is used by Spring Security during the authentication
     * process.</p>
     *
     * @param email the email address of the user to load
     * @return the user details associated with the given email
     * @throws UserNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetailsImpl loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(userDetailsMapper::toUserDetails)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Loads a user's security details by their identifier.
     *
     * @param userId the identifier of the user to load
     * @return the user details associated with the given identifier
     * @throws UserNotFoundException if no user is found with the given identifier
     */
    public UserDetailsImpl loadUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(userDetailsMapper::toUserDetails)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Retrieves the identifier of the currently authenticated user.
     *
     * <p>The authenticated principal must be an instance of
     * {@link UserDetailsImpl}.</p>
     *
     * @return the identifier of the currently authenticated user
     * @throws UnauthorizedException if no authenticated user is available
     *                               or if the authenticated principal is invalid
     */
    @Transactional(readOnly = true)
    public Long getPrincipalUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return Long.valueOf(authentication.getName());
    }
}
