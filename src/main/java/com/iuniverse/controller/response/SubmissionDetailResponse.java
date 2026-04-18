package com.iuniverse.controller.response;

import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class SubmissionDetailResponse {
    private Long submissionId;
    private Double totalScore;
    private Date submittedAt;
    private List<AnswerDetailResponse> answers;
}