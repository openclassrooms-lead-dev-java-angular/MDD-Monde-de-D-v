package com.openclassrooms.mddapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.config.TestSecurityConfig;
import com.openclassrooms.mddapi.security.TestAuthentication;
import com.openclassrooms.mddapi.user.service.UserService;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser
    void getMe_shouldReturnAuthenticatedUser() throws Exception {
        UserResponseDto response = mock(UserResponseDto.class);

        when(userService.getMe()).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk());

        verify(userService).getMe();
    }

    @Test
    void getMe_shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void patch_shouldUpdateUserWhenValidAndCsrfIsPresent() throws Exception {
        UpdateUserDto updateUserDto = createValidUpdateUserDto();
        UserResponseDto response = mock(UserResponseDto.class);

        when(userService.updateUser(any(UpdateUserDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(TestAuthentication.authenticatedUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk());

        verify(userService).updateUser(any(UpdateUserDto.class));
    }

    @Test
    void patch_shouldReturnForbiddenWhenCsrfIsMissing() throws Exception {
        UpdateUserDto updateUserDto = createValidUpdateUserDto();

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(TestAuthentication.authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void patch_shouldReturnBadRequestWhenDtoIsInvalid() throws Exception {
        UpdateUserDto updateUserDto = createInvalidUpdateUserDto();

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(TestAuthentication.authenticatedUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    private UpdateUserDto createValidUpdateUserDto() {
        return new UpdateUserDto(
                "user@email.com",
                "username",
                "Password/1234"
        );
    }

    private UpdateUserDto createInvalidUpdateUserDto() {
        return new UpdateUserDto(
                "user",
                "username",
                "pass"
        );
    }
}