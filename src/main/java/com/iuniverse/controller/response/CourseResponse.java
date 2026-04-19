package com.iuniverse.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CourseResponse {
    private Long id;
    private String courseName;
    private String description;
    private String joinCode;
    private String semesterName;
    private String instructorName; // Có thể nối First Name + Last Name

    private List<ModuleResponse> modules;
}