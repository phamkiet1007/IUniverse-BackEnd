package com.iuniverse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_student_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "student_response", columnDefinition = "TEXT")
    private String studentResponse;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "earned_points")
    private Double earnedPoints;
}