package com.iuniverse.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionRequest {

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<AnswerRequest> answers;
}