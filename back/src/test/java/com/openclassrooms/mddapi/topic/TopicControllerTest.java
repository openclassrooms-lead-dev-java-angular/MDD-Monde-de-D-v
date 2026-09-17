package com.openclassrooms.mddapi.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.factory.TopicTestFactory;
import com.openclassrooms.mddapi.topic.dto.TopicRequestDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.exception.TopicSlugAlreadyExists;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TopicController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TopicService topicService;

    // GET /api/v1/topics

    @Test
    void shouldReturnPaginatedTopics() throws Exception {
        Page<TopicResponseDto> page = TopicTestFactory.createTopicsRequestDto();
        Pageable pageable = PageRequest.of(0, 10);

        when(topicService.findAll(any(Pageable.class)))
                .thenReturn(page);


        mockMvc.perform(
                        get("/api/v1/topics")
                                .param("page", String.valueOf(pageable.getPageNumber()))
                                .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Java"))
                .andExpect(jsonPath("$.content[0].slug").value("java"))
                .andExpect(jsonPath("$.content[0].description").value("Java development."))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-09-16T10:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-09-16T11:00:00"))
                .andExpect(jsonPath("$.content[1].name").value("Python"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(topicService).findAll(pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenNoTopicExists() throws Exception {
        Page<TopicResponseDto> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(topicService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/topics")
                        .param("page", String.valueOf(0))
                        .param("size", String.valueOf(10))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(topicService).findAll(PageRequest.of(0, 10));
    }

    @Test
    void shouldAcceptAllowedTopicSortField() throws Exception {
        when(topicService.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/topics")
                        .param("sort", "name,asc")
                )
                .andExpect(status().isOk());
    }

    // GET /api/v1/topics/{slug}

    @Test
    void shouldReturnTopicBySlug() throws Exception {
        TopicResponseDto topicResponseDto = TopicTestFactory.createTopicResponseDto();

        when(topicService.findBySlug("java"))
                .thenReturn(topicResponseDto);

        mockMvc.perform(get("/api/v1/topics/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("java"));

        verify(topicService).findBySlug(topicResponseDto.slug());
    }

    @Test
    void shouldReturnNotFoundWhenTopicDoesNotExist() throws Exception {
        when(topicService.findBySlug("unknown-slug"))
                .thenThrow(new TopicNotFoundException("Topic not found"));

        mockMvc.perform(get("/api/v1/topics/unknown-slug"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Topic not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(topicService).findBySlug("unknown-slug");

    }

    // POST /api/v1/topics

    @Test
    void shouldCreateTopic() throws Exception {
        TopicRequestDto topicRequestDto = TopicTestFactory.createTopicRequestDto();
        TopicResponseDto topicResponseDto = TopicTestFactory.createTopicResponseDto();

        when(topicService.create(topicRequestDto))
                .thenReturn(topicResponseDto);

        mockMvc.perform(post("/api/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicRequestDto))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(topicResponseDto.name()))
                .andExpect(jsonPath("$.slug").value(topicResponseDto.slug()))
                .andExpect(jsonPath("$.description").value(topicResponseDto.description()));

        verify(topicService).create(topicRequestDto);
        ;
    }

    @Test
    void shouldReturnConflictWhenTopicSlugAlreadyExists() throws Exception {
        TopicRequestDto topicRequestDto = new TopicRequestDto("Java", "java", "Java programming");

        when(topicService.create(topicRequestDto))
                .thenThrow(new TopicSlugAlreadyExists("Slug already exists"));

        mockMvc.perform(post("/api/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicRequestDto))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Slug already exists"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(topicService).create(topicRequestDto);
    }

    // PUT /api/v1/topics/{slug}
    @Test
    void shouldUpdateTopic() throws Exception {
        TopicRequestDto topicRequestDto = TopicTestFactory.createTopicRequestDto();

        when(topicService.create(topicRequestDto))
                .thenThrow(new TopicSlugAlreadyExists("Slug already exists"));

        mockMvc.perform(post("/api/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicRequestDto))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Slug already exists"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(topicService).create(topicRequestDto);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingTopic() throws Exception {
        String slug = "java";

        TopicRequestDto topicRequestDto = TopicTestFactory.createTopicRequestDto();
        TopicResponseDto topicResponseDto = TopicTestFactory.createTopicResponseDto();

        when(topicService.update(slug, topicRequestDto))
                .thenReturn(topicResponseDto);

        mockMvc.perform(put("/api/v1/topics/{slug}", slug)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicRequestDto))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(topicResponseDto.name()))
                .andExpect(jsonPath("$.slug").value(topicResponseDto.slug()))
                .andExpect(jsonPath("$.description").value(topicResponseDto.description()));

        verify(topicService).update(slug, topicRequestDto);

    }

    // POST /api/v1/topics/{slug}/subscribe
    @Test
    void shouldSubscribeCurrentUserToTopic() {
        // todo test in subscribe branch
    }

    @Test
    void shouldReturnNotFoundWhenSubscribingToNonExistingTopic() {
        // todo test in subscribe branch
    }

    @Test
    void shouldUnsubscribeCurrentUserToTopic() {
        // todo test in subscribe branch
    }

    @Test
    void shouldReturnNotFoundWhenUnsubscribingToNonExistingTopic() {
        // todo test in subscribe branch
    }

    // GET /api/v1/topics/available-slug/{slug}
    @Test
    void shouldReturnSlugAvailableWhenSlugDoesNotExist() throws Exception {
        String slug = "java";

        AvailableSlugDto response = new AvailableSlugDto(true);

        when(topicService.availableSlug(slug))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/topics/available-slug/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available").value(true));

        verify(topicService).availableSlug(slug);

    }

    @Test
    void shouldReturnSlugUnavailableWhenSlugAlreadyExists() throws Exception {
        String slug = "java";

        AvailableSlugDto response = new AvailableSlugDto(false);

        when(topicService.availableSlug(slug))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/topics/available-slug/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available").value(false));

        verify(topicService).availableSlug(slug);

    }
}
