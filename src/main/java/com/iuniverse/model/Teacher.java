package com.iuniverse.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_teacher")
public class Teacher {

    @Id
    @Column(name = "user_id")
    private Long id;

    //@MapsId báo cho Hibernate biết: Hãy lấy Khóa chính của bảng tbl_user làm Khóa chính cho bảng này luôn
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "department", length = 255)
    private String department;
}