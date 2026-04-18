package com.iuniverse.controller.response;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class SubmissionSummaryResponse {
    private Long submissionId;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Date submittedAt;
    private Double totalScore;
}