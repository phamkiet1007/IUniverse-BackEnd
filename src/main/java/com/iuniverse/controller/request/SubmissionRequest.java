package com.iuniverse.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionRequest {

    private Long problemSetId;

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<AnswerRequest> answers;
    private String essayAnswer; 
}