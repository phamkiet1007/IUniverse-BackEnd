package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequest {
    @NotBlank(message = "Course name is required")
    private String courseName;

    private String description;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;
}