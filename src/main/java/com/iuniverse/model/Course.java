package com.iuniverse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends AbstractEntity {

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "semester_id")
    private Integer semesterId;

    @Column(name = "join_code", unique = true, nullable = false)
    private String joinCode;

    // Nếu bạn muốn lưu ai là giáo viên dạy lớp này
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
}