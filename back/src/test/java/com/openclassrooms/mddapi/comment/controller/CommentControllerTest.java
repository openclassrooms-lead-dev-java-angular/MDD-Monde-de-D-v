package com.openclassrooms.mddapi.comment.controller;

import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.service.CommentService;
import com.openclassrooms.mddapi.security.TestAuthentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.openclassrooms.mddapi.config.TestSecurityConfig;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

        when(commentService.create(
                org.mockito.ArgumentMatchers.eq(articlePublicId),
                any(CommentRequestDto.class)
        )).thenReturn(org.mockito.Mockito.mock(CommentResponseDto.class));

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
                org.mockito.ArgumentMatchers.eq(articlePublicId),
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
                org.mockito.ArgumentMatchers.anyString(),
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
                org.mockito.ArgumentMatchers.anyString(),
                any(CommentRequestDto.class)
        );
    }
}