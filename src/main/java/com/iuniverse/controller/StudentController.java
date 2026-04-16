package com.iuniverse.controller;

import com.iuniverse.controller.response.CourseResponse;
import com.iuniverse.model.Course;
import com.iuniverse.service.EnrollmentService;
import com.iuniverse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "Student Controller", description = "APIs for Students")
@Slf4j(topic = "STUDENT-CONTROLLER")
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @Operation(summary = "Enroll in a course", description = "Student uses Join Code to enter a course")
    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/course/enroll")
    public ResponseEntity<Object> enrollCourse(
            @RequestParam String joinCode,
            Authentication authentication
    ) {

        String username = authentication.getName();
        Long studentId = userService.findByUsername(username).getId();

        log.info("Student {} is enrolling with code: {}", username, joinCode);

        enrollmentService.enrollByCode(joinCode, studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Enrolled successfully! Welcome to the course!");

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Get my enrolled courses", description = "Student views the list of courses they have joined")
    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/my-courses")
    public ResponseEntity<Object> getMyCourses(Authentication authentication) {

        String username = authentication.getName();
        Long studentId = userService.findByUsername(username).getId();

        List<Course> courses = enrollmentService.getCoursesByStudent(studentId);

        List<CourseResponse> courseResponses = courses.stream().map(course -> {

            String teacherName = course.getInstructor().getUser().getFirstName() + " " +
                    course.getInstructor().getUser().getLastName();

            return CourseResponse.builder()
                    .id(course.getId())
                    .courseName(course.getCourseName())
                    .description(course.getDescription())
                    .joinCode(course.getJoinCode())
                    .instructorName(teacherName)
                    .build();
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Get all the courses of this student:!");

        result.put("data", courseResponses);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}