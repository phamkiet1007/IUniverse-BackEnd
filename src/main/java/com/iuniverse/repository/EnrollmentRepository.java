package com.iuniverse.repository;

import java.util.List;
import com.iuniverse.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Kiểm tra xem sinh viên đã có trong lớp chưa
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // Lấy danh sách ghi danh theo ID sinh viên
    List<Enrollment> findByStudentId(Long studentId);
}