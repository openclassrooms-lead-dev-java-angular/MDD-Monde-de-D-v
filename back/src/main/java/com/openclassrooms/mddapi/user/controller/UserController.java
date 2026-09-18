package com.openclassrooms.mddapi.user.controller;

import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import com.openclassrooms.mddapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto findById(
            @PathVariable Long id
    ) {
        return userService.getById();
    }

    @PatchMapping("/me")
    public UserResponseDto patch(
            @Valid @RequestBody UpdateUserDto userDto
    ) {
        return userService.updateUser(userDto);
    }

}
