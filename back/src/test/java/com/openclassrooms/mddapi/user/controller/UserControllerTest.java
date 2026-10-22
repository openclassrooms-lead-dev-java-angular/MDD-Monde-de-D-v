package com.openclassrooms.mddapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.factory.LocalDateTimeTestFactory;
import com.openclassrooms.mddapi.user.service.UserService;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    private static final String USER_ME_URL = "/api/v1/users/me";

    private static final String EMAIL = "john.doe@example.com";
    private static final String USERNAME = "John_Doe";

    private static final String UPDATED_EMAIL = "jane.doe@example.com";
    private static final String UPDATED_USERNAME = "Jane_Doe";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponseDto createUserResponseDto(
            String email,
            String username
    ) {
        return new UserResponseDto(
                email,
                username,
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt()
        );
    }

    @Test
    void shouldGetAuthenticatedUser() throws Exception {
        UserResponseDto response = createUserResponseDto(EMAIL, USERNAME);

        when(userService.getMe()).thenReturn(response);

        mockMvc.perform(get(USER_ME_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-09-16T10:00:00"))
                .andExpect(jsonPath("$.updatedAt")
                        .value("2026-09-16T11:00:00"));

        verify(userService).getMe();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldUpdateAuthenticatedUser() throws Exception {
        UpdateUserDto request = new UpdateUserDto(
                UPDATED_EMAIL,
                UPDATED_USERNAME
        );

        UserResponseDto response = createUserResponseDto(
                UPDATED_EMAIL,
                UPDATED_USERNAME
        );

        when(userService.updateUser(any(UpdateUserDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch(USER_ME_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(UPDATED_EMAIL))
                .andExpect(jsonPath("$.username").value(UPDATED_USERNAME))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-09-16T10:00:00"))
                .andExpect(jsonPath("$.updatedAt")
                        .value("2026-09-16T11:00:00"));

        ArgumentCaptor<UpdateUserDto> captor =
                ArgumentCaptor.forClass(UpdateUserDto.class);

        verify(userService).updateUser(captor.capture());

        assertThat(captor.getValue().email()).isEqualTo(UPDATED_EMAIL);
        assertThat(captor.getValue().username()).isEqualTo(UPDATED_USERNAME);

        verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        String invalidRequest = """
                {
                    "email": "email-invalide",
                    "username": "John_Doe"
                }
                """;

        mockMvc.perform(patch(USER_ME_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUsernameIsBlank() throws Exception {
        String invalidRequest = """
                {
                    "email": "john.doe@example.com",
                    "username": ""
                }
                """;

        mockMvc.perform(patch(USER_ME_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        String invalidRequest = """
                {
                    "email": "",
                    "username": "John_Doe"
                }
                """;

        mockMvc.perform(patch(USER_ME_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}