package com.iuniverse.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tbl_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_set_id", nullable = false)
    private ProblemSet problemSet;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private Date submittedAt;

    @Column(name = "total_score")
    private Double totalScore;

    // Quan hệ 1-N với StudentAnswer (1 bài nộp có nhiều câu trả lời)
    // CascadeType.ALL giúp khi lưu Submission thì nó tự lưu luôn list các câu trả lời
    @Builder.Default
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentAnswer> studentAnswers = new ArrayList<>();

    public void addStudentAnswer(StudentAnswer answer) {
        studentAnswers.add(answer);
        answer.setSubmission(this);
    }
}