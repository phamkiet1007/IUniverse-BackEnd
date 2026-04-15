package com.iuniverse.service;

import java.util.List;
import com.iuniverse.model.Course;

public interface EnrollmentService {
    // Chức năng ghi danh vào lớp bằng mã code
    void enrollByCode(String joinCode, Long studentId);

    // (Tùy chọn) Chức năng lấy danh sách các khóa học sinh viên đã tham gia
    // List<Course> getMyEnrolledCourses(Long studentId);
}