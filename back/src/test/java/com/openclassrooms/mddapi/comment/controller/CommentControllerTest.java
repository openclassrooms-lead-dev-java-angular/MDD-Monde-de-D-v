package com.openclassrooms.mddapi.comment.controller;

import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.service.CommentService;
import com.openclassrooms.mddapi.config.TestSecurityConfig;
import com.openclassrooms.mddapi.security.TestAuthentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void createComment_shouldReturnCreatedCommentWithValidCsrf()
            throws Exception {

        String articlePublicId = "article-public-id";

        String validJson = """
                {
                    "content": "This is a test comment"
                }
                """;

        CommentResponseDto response = new CommentResponseDto(
                "testuser",
                "This is a test comment",
                LocalDateTime.now()
        );

        when(commentService.create(
                eq(articlePublicId),
                any(CommentRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(post(
                        "/api/v1/articles/{publicId}/comments",
                        articlePublicId
                )
                        .with(TestAuthentication.authenticatedUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());

        verify(commentService).create(
                eq(articlePublicId),
                any(CommentRequestDto.class)
        );
    }

    @Test
    void createComment_shouldReturnForbiddenWithoutCsrf()
            throws Exception {

        String articlePublicId = "article-public-id";

        String validJson = """
                {
                    "content": "This is a test comment"
                }
                """;

        mockMvc.perform(post(
                        "/api/v1/articles/{publicId}/comments",
                        articlePublicId
                )
                        .with(TestAuthentication.authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isForbidden());

        verify(commentService, never()).create(
                anyString(),
                any(CommentRequestDto.class)
        );
    }

    @Test
    void createComment_shouldReturnBadRequestWhenContentIsMissing()
            throws Exception {

        String articlePublicId = "article-public-id";

        String invalidJson = """
                {
                    "content": ""
                }
                """;

        mockMvc.perform(post(
                        "/api/v1/articles/{publicId}/comments",
                        articlePublicId
                )
                        .with(TestAuthentication.authenticatedUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).create(
                anyString(),
                any(CommentRequestDto.class)
        );
    }
}
