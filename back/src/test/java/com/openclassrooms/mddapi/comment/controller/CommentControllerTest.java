package com.openclassrooms.mddapi.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.service.CommentService;
import com.openclassrooms.mddapi.factory.CommentTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetArticleComments() throws Exception {
        String articleSlug = "mon-article";
        CommentResponseDto commentResponseDto = CommentTestFactory.createCommentResponseDto();
        Page<CommentResponseDto> response = new PageImpl<>(
                List.of(commentResponseDto),
                PageRequest.of(0, 10),
                1);

        when(commentService.getArticleComments(
                any(Pageable.class),
                eq(articleSlug))
        )
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/comments/article/{articleSlug}",
                                articleSlug
                        )
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content")
                                .isArray())
                .andExpect(
                        jsonPath("$.content[0].username")
                                .value(response.getContent().getFirst().username()))
                .andExpect(
                        jsonPath("$.content[0].content")
                                .value(response.getContent().getFirst().content()));

        verify(commentService).getArticleComments(
                any(Pageable.class),
                eq(articleSlug));
    }

    @Test
    void shouldCreateComment() throws Exception {
        String articleSlug = "my-article";
        CommentRequestDto request = CommentTestFactory.createCommentRequestDto();
        CommentResponseDto response = CommentTestFactory.createCommentResponseDto();

        when(commentService.create(
                eq(articleSlug),
                any(CommentRequestDto.class))
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/comments/article/{articleSlug}", articleSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.username")
                                .value(response.username()))
                .andExpect(
                        jsonPath("$.content")
                                .value(response.content()));

        verify(commentService).create(
                eq(articleSlug),
                any(CommentRequestDto.class));
    }

    @Test
    void shouldRejectBlankComment() throws Exception {
        CommentRequestDto request = new CommentRequestDto("");

        mockMvc.perform(
                        post("/api/v1/comments/article/{articleSlug}",
                                "my-article")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isInternalServerError());

        verify(commentService, never()).create(
                anyString(),
                any(CommentRequestDto.class));
    }
}
