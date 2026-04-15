package com.iuniverse.controller;

import com.iuniverse.model.Course;
import com.iuniverse.service.EnrollmentService;
import com.iuniverse.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enroll")
@RequiredArgsConstructor
@Slf4j(topic = "ENROLLMENT-CONTROLLER")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

    // =========================
    // ENROLL COURSE BY CODE
    // =========================
    @PostMapping("/join")
    public ResponseEntity<Object> enrollCourse(
            @RequestParam String joinCode,
            Authentication authentication
    ) {

        String username = authentication.getName();
        Long studentId = userService.findByUsername(username).getId();

        log.info("Student {} joining course with code {}", username, joinCode);

        enrollmentService.enrollByCode(joinCode, studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 200);
        result.put("message", "Enrolled successfully");

        return ResponseEntity.ok(result);
    }

    // =========================
    // GET MY COURSES (STUDENT)
    // =========================
    @GetMapping("/my-courses")
    public ResponseEntity<Object> getMyCourses(Authentication authentication) {

        String username = authentication.getName();
        Long studentId = userService.findByUsername(username).getId();

        List<Course> courses = enrollmentService.getCoursesByStudent(studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 200);
        result.put("message", "My courses");
        result.put("data", courses);

        return ResponseEntity.ok(result);
    }
}