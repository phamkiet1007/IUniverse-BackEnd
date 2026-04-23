package com.iuniverse.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "tbl_rating")
@Data
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;
    private Long studentId;
    
    @Column(name = "score", nullable = false)
    private Integer starCount; // 1-5

    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();
    
}