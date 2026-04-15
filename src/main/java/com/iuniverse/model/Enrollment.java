// model/Enrollment.java
package com.iuniverse.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Table(name = "tbl_enrollment")
@Data
public class Enrollment extends AbstractEntity { // Kế thừa để có id, createdDate nếu cần

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student; // Hoặc Student student tùy cách bạn map User

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDateTime enrollDate = LocalDateTime.now();
}