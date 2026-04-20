package com.iuniverse.controller.response;

import com.iuniverse.common.QuestionType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionResponse {
    private Long id;
    private String content;
    private QuestionType type;
    private Double points;
    private String correctAns;

    // Hứng danh sách các lựa chọn (A,B,C,D) từ bảng tbl_question_option
    private List<String> options;
}