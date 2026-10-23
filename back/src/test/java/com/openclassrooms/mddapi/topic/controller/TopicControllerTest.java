package com.openclassrooms.mddapi.topic.controller;

import com.openclassrooms.mddapi.config.TestSecurityConfig;
import com.openclassrooms.mddapi.factory.LocalDateTimeTestFactory;
import com.openclassrooms.mddapi.security.TestAuthentication;
import com.openclassrooms.mddapi.topic.dto.TopicOptionDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TopicController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TopicService topicService;

    @Test
    void findAll_shouldReturnPaginatedTopics() throws Exception {
        TopicResponseDto topic = new TopicResponseDto(
                UUID.randomUUID().toString(),
                "Java",
                "Java programming",
                true,
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt()
        );

        Page<TopicResponseDto> page = new PageImpl<>(
                List.of(topic),
                PageRequest.of(0, 10),
                1
        );

        when(topicService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/topics")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc")
                        .with(TestAuthentication.authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].name").value("Java"))
                .andExpect(jsonPath("$.content[0].description").value("Java programming"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));

        verify(topicService).findAll(any(Pageable.class));
    }

    @Test
    void getTopicOptions_shouldReturnTopicOptions() throws Exception {
        TopicOptionDto option = new TopicOptionDto(
                "topic-public-id",
                "Java"
        );

        when(topicService.getOptions()).thenReturn(List.of(option));

        mockMvc.perform(get("/api/v1/topics/options")
                        .with(TestAuthentication.authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].publicId").value("topic-public-id"))
                .andExpect(jsonPath("$[0].name").value("Java"));

        verify(topicService).getOptions();
    }

    @Test
    void getSubscribedTopics_shouldReturnPaginatedTopics() throws Exception {
        TopicResponseDto topic = new TopicResponseDto(
                UUID.randomUUID().toString(),
                "Java",
                "Java programming",
                true,
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt()
        );

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
                        .with(TestAuthentication.authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Java"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(topicService).getSubscribedTopics(any(Pageable.class));
    }

    @Test
    void subscribe_shouldReturnCreated() throws Exception {
        String publicId = "topic-public-id";

        doNothing().when(topicService).subscribe(publicId);

        mockMvc.perform(post("/api/v1/topics/{publicId}/subscribe", publicId)
                        .with(TestAuthentication.authenticatedUser())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated());

        verify(topicService).subscribe(publicId);
    }

    @Test
    void unsubscribe_shouldReturnNoContent() throws Exception {
        String publicId = "topic-public-id";

        doNothing().when(topicService).unsubscribe(publicId);

        mockMvc.perform(delete("/api/v1/topics/{publicId}/subscribe", publicId)
                        .with(TestAuthentication.authenticatedUser())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(topicService).unsubscribe(publicId);
    }
}