package com.openclassrooms.mddapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.factory.MediaTestFactory;
import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.user.service.UserService;
import com.openclassrooms.mddapi.user.dto.UpdateUserDto;
import com.openclassrooms.mddapi.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void getMeShouldReturnUserResponseDto() throws Exception {
        UserResponseDto userDto =
                UserTestFactory.createUserResponseDto();

        when(userService.getMe())
                .thenReturn(userDto);

        mockMvc.perform(
                        get("/api/v1/users/me")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.email").value(userDto.email()))
                .andExpect(jsonPath("$.username").value(userDto.username()))
                .andExpect(jsonPath("$.firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.avatar").value(userDto.avatar()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.createdAt").value("2026-06-01T10:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-07-01T10:00:00"));

        verify(userService).getMe();
    }

    @Test
    void updateShouldReturnUpdatedUserResponseDtoWithMedia()
            throws Exception {

        UpdateUserDto updateUserDto =
                UserTestFactory.createUpdateUserDto(false);

        MockMultipartFile userPart = new MockMultipartFile(
                "user",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateUserDto)
        );

        MockMultipartFile media =
                MediaTestFactory.createMedia();

        UserResponseDto userDto =
                UserTestFactory.createUserResponseDto();

        when(userService.updateUser(updateUserDto, media))
                .thenReturn(userDto);

        mockMvc.perform(
                        multipart("/api/v1/users/me")
                                .file(userPart)
                                .file(media)
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.email").value(userDto.email()))
                .andExpect(jsonPath("$.username").value(userDto.username()))
                .andExpect(jsonPath("$.firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.avatar").value(userDto.avatar()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.createdAt").value("2026-06-01T10:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-07-01T10:00:00"));

        verify(userService).updateUser(updateUserDto, media);
    }

    @Test
    void updateShouldReturnUpdatedUserResponseDtoWithoutMedia()
            throws Exception {

        UpdateUserDto updateUserDto =
                UserTestFactory.createUpdateUserDto(false);

        MockMultipartFile userPart = new MockMultipartFile(
                "user",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateUserDto)
        );

        UserResponseDto userDto =
                UserTestFactory.createUserResponseDto();

        when(userService.updateUser(updateUserDto, null))
                .thenReturn(userDto);

        mockMvc.perform(
                        multipart("/api/v1/users/me")
                                .file(userPart)
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.email").value(userDto.email()))
                .andExpect(jsonPath("$.username").value(userDto.username()))
                .andExpect(jsonPath("$.firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.avatar").value(userDto.avatar()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.createdAt").value("2026-06-01T10:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-07-01T10:00:00"));

        verify(userService).updateUser(updateUserDto, null);
    }

    @Test
    void updateShouldReturnBadRequestWhenDtoIsInvalid()
            throws Exception {

        UpdateUserDto invalidDto = new UpdateUserDto(
                "",
                "",
                "",
                "",
                false
        );

        MockMultipartFile userPart = new MockMultipartFile(
                "user",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(invalidDto)
        );

        mockMvc.perform(
                        multipart("/api/v1/users/me")
                                .file(userPart)
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                )
                .andExpect(status().isInternalServerError());

        verify(userService, never())
                .updateUser(any(), any());
    }
}