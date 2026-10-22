package com.openclassrooms.mddapi.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.service.CommentService;
import com.openclassrooms.mddapi.factory.LocalDateTimeTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CommentControllerTest {

    private static final String ARTICLE_SLUG = "test-article";
    private static final String CONTENT = "Mon commentaire de test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @Test
    void shouldCreateComment() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(CONTENT);

        CommentResponseDto responseDto = new CommentResponseDto(
                "John_Doe",
                CONTENT,
                LocalDateTimeTestFactory.generateCreatedAt()
        );

        when(commentService.create(
                eq(ARTICLE_SLUG),
                any(CommentRequestDto.class)
        )).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/articles/{slug}/comments", ARTICLE_SLUG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John_Doe"))
                .andExpect(jsonPath("$.content").value(CONTENT))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(commentService).create(
                eq(ARTICLE_SLUG),
                any(CommentRequestDto.class)
        );
    }

    @Test
    void shouldPassArticleSlugAndCommentContentToService() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(CONTENT);

        when(commentService.create(
                eq(ARTICLE_SLUG),
                any(CommentRequestDto.class)
        )).thenReturn(new CommentResponseDto(
                "John_Doe",
                CONTENT,
                LocalDateTimeTestFactory.generateCreatedAt()
        ));

        mockMvc.perform(post("/api/v1/articles/{slug}/comments", ARTICLE_SLUG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<CommentRequestDto> captor =
                org.mockito.ArgumentCaptor.forClass(CommentRequestDto.class);

        verify(commentService).create(eq(ARTICLE_SLUG), captor.capture());

        org.assertj.core.api.Assertions.assertThat(captor.getValue().content())
                .isEqualTo(CONTENT);
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        String invalidRequest = """
                {
                    "content": ""
                }
                """;

        mockMvc.perform(post("/api/v1/articles/{slug}/comments", ARTICLE_SLUG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}