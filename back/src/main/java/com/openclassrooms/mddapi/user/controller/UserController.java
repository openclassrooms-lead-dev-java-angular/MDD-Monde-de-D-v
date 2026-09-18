package com.openclassrooms.mddapi.user.controller;

import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import com.openclassrooms.mddapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for user-related operations.
 *
 * <p>Provides endpoints for retrieving and updating information
 * about the currently authenticated user.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the currently authenticated user's information.
     *
     * @return the authenticated user's information
     */
    @GetMapping("/me")
    public UserResponseDto getMe() {
        return userService.getMe();
    }

    /**
     * Updates the currently authenticated user's information.
     *
     * @param userDto the data to update
     * @return the updated user's information
     */
    @PatchMapping("/me")
    public UserResponseDto patch(
            @Valid @RequestBody UpdateUserDto userDto
    ) {
        return userService.updateUser(userDto);
    }

}
