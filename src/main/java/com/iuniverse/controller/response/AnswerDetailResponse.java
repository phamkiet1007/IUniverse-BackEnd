package com.iuniverse.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerDetailResponse {
    private Long questionId;
    private String questionContent;
    private String studentResponse; // Student chọn gì
    private String correctAnswer;   // Đáp án đúng là gì
    private Boolean isCorrect;      // Đúng hay sai
    private Double earnedPoints;    // Được mấy điểm
}