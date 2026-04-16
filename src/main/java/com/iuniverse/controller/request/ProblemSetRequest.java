package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProblemSetRequest {
    @NotBlank(message = "title of problem set cannot be blank")
    private String title;

    private String description;

    @NotNull(message = "Due date cannot be blank")
    private LocalDateTime dueDate;

    private Integer timeLimitMins;

    private List<QuestionRequest> questions;
}