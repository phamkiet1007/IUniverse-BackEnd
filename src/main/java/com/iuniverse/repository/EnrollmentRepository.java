package com.iuniverse.repository;

import com.iuniverse.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // check duplicate
    boolean existsByStudentUserIdAndCourseId(Long studentId, Long courseId);

    // teacher dùng
    List<Enrollment> findByCourseId(Long courseId);

    // thêm dòng này cho student
    List<Enrollment> findByStudentUserId(Long studentId);

    boolean existsByCourseIdAndStudent_UserId(Long courseId, Long studentId);
}