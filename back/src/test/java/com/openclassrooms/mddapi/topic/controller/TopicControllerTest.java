package com.openclassrooms.mddapi.topic.controller;

import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.service.TopicService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TopicController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TopicService topicService;

    // -------------------------------------------------------------------------
    // GET /api/v1/topics
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnPaginatedTopics() throws Exception {
        TopicResponseDto topic = org.mockito.Mockito.mock(TopicResponseDto.class);

        Page<TopicResponseDto> page = new PageImpl<>(
                List.of(topic),
                PageRequest.of(0, 10),
                1
        );

        when(topicService.findAll(
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class
                )
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/topics")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(topicService).findAll(
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 0
                                && pageable.getPageSize() == 10
                                && pageable.getSort().getOrderFor("name") != null
                                && Objects.requireNonNull(pageable.getSort().getOrderFor("name")).isAscending()
                )
        );

        verifyNoMoreInteractions(topicService);
    }

    @Test
    void shouldReturnEmptyPageWhenNoTopicsExist() throws Exception {
        when(topicService.findAll(
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class
                )
        )).thenReturn(Page.empty(PageRequest.of(0, 10)));

        mockMvc.perform(get("/api/v1/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(topicService).findAll(
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class
                )
        );
    }

    @Test
    void shouldPassSortParameterToService() throws Exception {
        when(topicService.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/topics")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(topicService).findAll(pageableCaptor.capture());

        assertEquals(
                Sort.Direction.ASC,
                Objects.requireNonNull(pageableCaptor.getValue()
                                .getSort()
                                .getOrderFor("name"))
                        .getDirection()
        );
    }

// -------------------------------------------------------------------------
// GET /api/v1/topics/options
// -------------------------------------------------------------------------

    @Test
    void shouldReturnTopicOptions() throws Exception {
        TopicOptionDto option = org.mockito.Mockito.mock(TopicOptionDto.class);

        when(topicService.getOptions()).thenReturn(List.of(option));

        mockMvc.perform(get("/api/v1/topics/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(topicService).getOptions();
        verifyNoMoreInteractions(topicService);
    }

    @Test
    void shouldReturnEmptyListWhenNoTopicOptionsExist() throws Exception {
        when(topicService.getOptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/topics/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(topicService).getOptions();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/topics/{slug}/subscribe
    // -------------------------------------------------------------------------

    @Test
    void shouldSubscribeToTopic() throws Exception {
        String slug = "java";

        mockMvc.perform(post("/api/v1/topics/{slug}/subscribe", slug))
                .andExpect(status().isCreated());

        verify(topicService).subscribe(slug);
        verifyNoMoreInteractions(topicService);
    }

    @Test
    void shouldPropagateSubscribeServiceException() throws Exception {
        String slug = "unknown-topic";

        doThrow(new TopicNotFoundException(
                "Topic not found with slug '" + slug + "'"
        )).when(topicService).subscribe(slug);

        mockMvc.perform(post("/api/v1/topics/{slug}/subscribe", slug))
                .andExpect(status().isNotFound());

        verify(topicService).subscribe(slug);
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/topics/{slug}/subscribe
    // -------------------------------------------------------------------------

    @Test
    void shouldUnsubscribeFromTopic() throws Exception {
        String slug = "java";

        mockMvc.perform(delete("/api/v1/topics/{slug}/subscribe", slug))
                .andExpect(status().isNoContent());

        verify(topicService).unsubscribe(slug);
        verifyNoMoreInteractions(topicService);
    }

    @Test
    void shouldPropagateUnsubscribeServiceException() throws Exception {
        String slug = "unknown-topic";

        doThrow(new TopicNotFoundException(
                "Topic not found with slug '" + slug + "'"
        )).when(topicService).unsubscribe(slug);

        mockMvc.perform(delete("/api/v1/topics/{slug}/subscribe", slug))
                .andExpect(status().isNotFound());

        verify(topicService).unsubscribe(slug);
    }

    // -------------------------------------------------------------------------
// GET /api/v1/topics/subscribed
// -------------------------------------------------------------------------

    @Test
    void shouldReturnPaginatedSubscribedTopics() throws Exception {
        TopicResponseDto topic = org.mockito.Mockito.mock(TopicResponseDto.class);

        Page<TopicResponseDto> page = new PageImpl<>(
                List.of(topic),
                PageRequest.of(0, 10),
                1
        );

        when(topicService.getSubscribedTopics(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/topics/subscribed")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(topicService).getSubscribedTopics(argThat(pageable ->
                pageable.getPageNumber() == 0
                        && pageable.getPageSize() == 10
                        && pageable.getSort().getOrderFor("name") != null
                        && Objects.requireNonNull(
                        pageable.getSort().getOrderFor("name")
                ).isAscending()
        ));

        verifyNoMoreInteractions(topicService);
    }

    @Test
    void shouldReturnEmptyPageWhenNoSubscribedTopicsExist() throws Exception {
        when(topicService.getSubscribedTopics(any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        mockMvc.perform(get("/api/v1/topics/subscribed")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(topicService).getSubscribedTopics(any(Pageable.class));
        verifyNoMoreInteractions(topicService);
    }

    @Test
    void shouldPassSortParameterToSubscribedTopicsService() throws Exception {
        when(topicService.getSubscribedTopics(any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/topics/subscribed")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(topicService).getSubscribedTopics(pageableCaptor.capture());

        assertEquals(
                Sort.Direction.ASC,
                Objects.requireNonNull(
                        pageableCaptor.getValue()
                                .getSort()
                                .getOrderFor("name")
                ).getDirection()
        );
    }
}
