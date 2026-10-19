package com.openclassrooms.mddapi.topic.controller;

import com.openclassrooms.mddapi.factory.TopicTestFactory;
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

import static org.hamcrest.Matchers.hasSize;
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
        Page<TopicResponseDto> page =
                TopicTestFactory.createPageableTopicsResponseDto();

        when(topicService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/topics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Java"))
                .andExpect(jsonPath("$.content[0].slug").value("java"))
                .andExpect(jsonPath("$.content[0].description")
                        .value("Java development."))
                .andExpect(jsonPath("$.content[0].createdAt")
                        .value("2026-09-16T10:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt")
                        .value("2026-09-16T11:00:00"))
                .andExpect(jsonPath("$.content[1].name").value("Python"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(topicService).findAll(PageRequest.of(0, 10));
    }

    @Test
    void shouldReturnEmptyPageWhenNoTopicExists() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        when(topicService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/api/v1/topics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(topicService).findAll(pageable);
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
        when(topicService.getOptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/topics/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(topicService).getOptions();
    }

// -------------------------------------------------------------------------
// POST /api/v1/topics/{slug}/subscribe
// -------------------------------------------------------------------------

    @Test
    void shouldSubscribeCurrentUserToTopic() throws Exception {
        mockMvc.perform(post("/api/v1/topics/java/subscribe"))
                .andExpect(status().isCreated());

        verify(topicService).subscribe("java");
    }

    @Test
    void shouldReturnNotFoundWhenSubscribingToUnknownTopic()
            throws Exception {
        doThrow(new TopicNotFoundException("Topic not found"))
                .when(topicService).subscribe("unknown-topic");

        mockMvc.perform(post("/api/v1/topics/unknown-topic/subscribe"))
                .andExpect(status().isNotFound());

        verify(topicService).subscribe("unknown-topic");
    }

// -------------------------------------------------------------------------
// DELETE /api/v1/topics/{slug}/subscribe
// -------------------------------------------------------------------------

    @Test
    void shouldUnsubscribeCurrentUserFromTopic() throws Exception {
        mockMvc.perform(delete("/api/v1/topics/java/subscribe"))
                .andExpect(status().isNoContent());

        verify(topicService).unsubscribe("java");
    }

    @Test
    void shouldReturnNotFoundWhenUnsubscribingFromUnknownTopic()
            throws Exception {
        doThrow(new TopicNotFoundException("Topic not found"))
                .when(topicService).unsubscribe("unknown-topic");

        mockMvc.perform(delete("/api/v1/topics/unknown-topic/subscribe"))
                .andExpect(status().isNotFound());

        verify(topicService).unsubscribe("unknown-topic");
    }
}
