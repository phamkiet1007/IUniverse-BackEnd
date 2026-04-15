package com.iuniverse.service.impl;

import com.iuniverse.model.Course;
import com.iuniverse.model.Enrollment;
import com.iuniverse.model.User;
import com.iuniverse.repository.CourseRepository;
import com.iuniverse.repository.EnrollmentRepository;
import com.iuniverse.repository.UserRepository;
import com.iuniverse.service.EnrollmentService;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional // Nên thêm cái này để đảm bảo dữ liệu nhất quán khi lưu
    public void enrollByCode(String joinCode, Long studentId) {
        // 1. Tìm lớp theo mã join_code
        Course course = courseRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với mã: " + joinCode));

        // 2. Kiểm tra xem sinh viên đã tham gia lớp này chưa
        // Ép kiểu (Long) vì AbstractEntity trả về Serializable
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, (Long) course.getId())) {
            throw new InvalidDataException("Bạn đã tham gia lớp học này rồi!");
        }

        // 3. Tìm thông tin sinh viên
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên!"));

        // 4. Tạo bản ghi ghi danh mới
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        
        enrollmentRepository.save(enrollment);
    }
}