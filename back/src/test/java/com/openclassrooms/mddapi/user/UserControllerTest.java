package com.openclassrooms.mddapi.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.factory.UserTestFactory;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;


    @Test
    void getUserByIdSouldReturnUserResponseDto() throws Exception {
        UserResponseDto userDto = UserTestFactory.createUserResponseDto();

        when(userService.getById(1L))
                .thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.email").value(userDto.email()))
                .andExpect(jsonPath("$.username").value(userDto.username()))
                .andExpect(jsonPath("$.firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.lastLoginAt").value("2026-09-01T10:00:00"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-01T10:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-07-01T10:00:00"));

        verify(userService).getById(userDto.id());
    }


    @Test
    void updateSouldReturnUpdatedUserResponseDto() throws Exception {
        UpdateUserDto updateUserDto =
                UserTestFactory.createUpdateUserDto();

        UserResponseDto userDto =
                UserTestFactory.createUserResponseDto();

        when(userService.updateUser(1L, updateUserDto))
                .thenReturn(userDto);

        mockMvc.perform(
                        patch("/api/v1/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateUserDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.email").value(userDto.email()))
                .andExpect(jsonPath("$.username").value(userDto.username()))
                .andExpect(jsonPath("$.firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.lastLoginAt").value("2026-09-01T10:00:00"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-01T10:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-07-01T10:00:00"));

        verify(userService).updateUser(1L, updateUserDto);
    }
}
