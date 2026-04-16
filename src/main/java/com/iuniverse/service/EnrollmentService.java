package com.iuniverse.service;

import com.iuniverse.exception.InvalidDataException;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Course;
import com.iuniverse.model.Enrollment;
import com.iuniverse.model.Student;
import com.iuniverse.repository.CourseRepository;
import com.iuniverse.repository.EnrollmentRepository;
import com.iuniverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ENROLLMENT-SERVICE")
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;

    //DÀNH CHO SINH VIÊN: Dùng Join Code để vào lớp
    @Transactional
    public void enrollCourse(String joinCode, Long currentStudentId) {

        //Tìm lớp học theo Join Code
        Course course = courseRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find the course with joincode: " + joinCode));

        // Nếu đã tham gia rồi thì chặn lại
        if (enrollmentRepository.existsByStudentUserIdAndCourseId(currentStudentId, course.getId())) {
            throw new InvalidDataException("You've already enrolled in this course!");
        }

        // Lấy profile Sinh viên
        Student student = userRepository.findById(currentStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getStudentProfile();

        if (student == null) {
            throw new AccessDeniedException("You are not a student!");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status("ACTIVE")
                .build();

        enrollmentRepository.save(enrollment);
        log.info("Student {} enrolled in Course {}", currentStudentId, course.getId());
    }

    //DÀNH CHO GIẢNG VIÊN: Lấy danh sách lớp
    public List<String> getStudentsInCourse(Long courseId, Long currentTeacherId) {

        Course course = courseService.getValidCourseForTeacher(courseId, currentTeacherId);

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());

        return enrollments.stream()
                .map(e -> e.getStudent().getStudentCode() + " - " +
                        e.getStudent().getUser().getFirstName() + " " +
                        e.getStudent().getUser().getLastName())
                .collect(Collectors.toList());
    }
}