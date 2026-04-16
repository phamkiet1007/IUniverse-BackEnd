package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionRequest {
    @NotBlank(message = "content of question cannot be blank")
    private String content;

    @NotBlank(message = "type of question cannot be blank")
    private String type;

    @NotBlank(message = "correct answer cannot be blank")
    private String correctAns;

    @NotNull(message = "point of question cannot be blank")
    private Double points;

    private List<String> options;
}