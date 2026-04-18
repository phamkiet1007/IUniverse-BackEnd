package com.iuniverse.model;

import com.iuniverse.common.QuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "tbl_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_set_id", referencedColumnName = "id", nullable = false)
    private ProblemSet problemSet;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private QuestionType type;

    @Column(name = "correct_ans", nullable = false, columnDefinition = "TEXT")
    private String correctAns;

    @Column(name = "points", columnDefinition = "DOUBLE PRECISION DEFAULT 1.0")
    private Double points;

    @ElementCollection
    @CollectionTable(name = "tbl_question_option", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options;
}