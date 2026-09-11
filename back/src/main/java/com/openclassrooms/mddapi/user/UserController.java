package com.openclassrooms.mddapi.user;

import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponseDto findById(
            @PathVariable Long id
    ) throws NotFoundException {
        return userService.getById(id);
    }

    @PatchMapping("/{id}")
    public UserResponseDto patch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDto userDto
    ) {
        return userService.updateUser(id, userDto);
    }

}
