package com.iuniverse.repository;

import com.iuniverse.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    //Kiểm tra xem sinh viên đã có trong lớp chưa để tránh việc join 2 lần
    boolean existsByStudentUserIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByCourseId(Long courseId);
}