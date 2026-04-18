package com.iuniverse.controller.request;

import lombok.Data;
import java.util.List;

@Data
public class GradeRequest {
    private List<AnswerGrade> grades;

    @Data
    public static class AnswerGrade {
        private Long studentAnswerId;
        private Double score;
        private String teacherComment;
    }
}