package com.iuniverse.service.impl;

import com.iuniverse.exception.InvalidDataException;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Course;
import com.iuniverse.model.Enrollment;
import com.iuniverse.model.Student;
import com.iuniverse.repository.CourseRepository;
import com.iuniverse.repository.EnrollmentRepository;
import com.iuniverse.repository.UserRepository;
import com.iuniverse.service.EnrollmentService;

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
public class EnrollmentServiceImpl implements EnrollmentService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    // ✅ STUDENT: Enroll by join code
    @Override
    @Transactional
    public void enrollByCode(String joinCode, Long studentId) {

        if (joinCode == null || joinCode.trim().isEmpty()) {
            throw new InvalidDataException("Invalid join code!");
        }

        String code = joinCode.trim(); // 🔥 fix lỗi final

        // 1. Find course
        Course course = courseRepository.findByJoinCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with code: " + code));

        // 2. Check duplicate
        if (enrollmentRepository.existsByStudentUserIdAndCourseId(studentId, course.getId())) {
            throw new InvalidDataException("You have already enrolled in this course!");
        }

        // 3. Get student profile
        Student student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getStudentProfile();

        if (student == null) {
            throw new AccessDeniedException("You are not a student!");
        }

        // 4. Save enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status("ACTIVE")
                .build();

        enrollmentRepository.save(enrollment);

        log.info("Student {} enrolled in Course {}", studentId, course.getId());
    }

    // ✅ STUDENT: Get my courses
    @Override
    public List<Course> getCoursesByStudent(Long studentId) {

        return enrollmentRepository.findByStudentUserId(studentId)
                .stream()
                .map(Enrollment::getCourse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getStudentsInCourse(Long courseId, Long teacherId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getInstructor().getUser().getId().equals(teacherId)) {
            throw new AccessDeniedException("Access denied! You do not have permission to view this course.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        return enrollments.stream().map(enrollment -> {
            Student student = enrollment.getStudent();
            String fullName = student.getUser().getFirstName() + " " + student.getUser().getLastName();

            return student.getStudentCode() + " - " + fullName;
        }).collect(Collectors.toList());
    }
}