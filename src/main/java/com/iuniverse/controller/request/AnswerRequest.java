package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {

    @NotNull(message = "Question ID cannot be null")
    private Long questionId;

    // Chứa đáp án sinh viên chọn (A, B, C, D hoặc nội dung text điền vào)
    private String studentResponse;
}