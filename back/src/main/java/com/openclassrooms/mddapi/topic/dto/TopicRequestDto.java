package com.openclassrooms.mddapi.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TopicRequestDto(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 90)
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must contain only lowercase letters, numbers and hyphens"
        )
        String slug,

        @Size(max = 255)
        String description
) { }
