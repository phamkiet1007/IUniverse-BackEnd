package com.iuniverse.service;

import com.iuniverse.controller.request.CourseRequest;
import com.iuniverse.controller.response.CourseResponse;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Course;
import com.iuniverse.model.Semester;
import com.iuniverse.model.Teacher;
import com.iuniverse.repository.CourseRepository;
import com.iuniverse.service.SemesterService;
import com.iuniverse.service.TeacherService;  // Import Service
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;

    private final SemesterService semesterService;

    private final TeacherService teacherService;

    @Transactional
    public Long createCourse(CourseRequest req, Long currentUserId) {

        Teacher instructor = teacherService.getTeacherEntityById(currentUserId);

        Semester semester = semesterService.getSemesterEntityById(req.getSemesterId());

        //create the unique join code
        String joinCode = generateUniqueJoinCode();

        Course course = Course.builder()
                .courseName(req.getCourseName())
                .description(req.getDescription())
                .instructor(instructor)
                .semester(semester)
                .joinCode(joinCode)
                .build();

        return courseRepository.save(course).getId();
    }

    private String generateUniqueJoinCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;

        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            code = sb.toString();
        } while (courseRepository.existsByJoinCode(code));

        return code;
    }

    @Transactional(readOnly = true) // Tối ưu hóa tốc độ đọc
    public List<CourseResponse> getMyCourses(Long teacherId) {

        Teacher instructor = teacherService.getTeacherEntityById(teacherId);

        //find all courses of this teacher
        List<Course> courses = courseRepository.findAllByInstructor(instructor);

        //Map from Entity to DTO to hide private in4
        return courses.stream().map(course -> {
            String fullName = course.getInstructor().getUser().getFirstName() + " " +
                    course.getInstructor().getUser().getLastName();

            return CourseResponse.builder()
                    .id(course.getId())
                    .courseName(course.getCourseName())
                    .description(course.getDescription())
                    .joinCode(course.getJoinCode())
                    .semesterName(course.getSemester().getName())
                    .instructorName(fullName)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseDetail(Long courseId, Long currentUserId) {
        log.info("Fetching details for course ID: {} by User ID: {}", courseId, currentUserId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!course.getInstructor().getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to access unauthorized course {}", currentUserId, courseId);
            throw new AccessDeniedException("You do not have permission to view this course's details.");
        }

        //Map to DTO
        String fullName = course.getInstructor().getUser().getFirstName() + " " +
                course.getInstructor().getUser().getLastName();

        return CourseResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .joinCode(course.getJoinCode())
                .semesterName(course.getSemester().getName())
                .instructorName(fullName)
                .build();
    }

    @Transactional
    public void updateCourse(Long courseId, CourseRequest req, Long currentTeacherId) {
        log.info("Teacher {} is updating Course {}", currentTeacherId, courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!course.getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied! You are not authorized");
        }

        course.setCourseName(req.getCourseName());
        course.setDescription(req.getDescription());

        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId, Long currentTeacherId) {
        log.info("Teacher {} is deleting Course {}", currentTeacherId, courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!course.getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied! You are not authorized");
        }

        //ON DELETE CASCADE trong SQL, khi xóa Course,
        //toàn bộ Module, Material, ProblemSet, Question, Option bên trong sẽ tự động "bay màu" theo!
        courseRepository.delete(course);
    }
}