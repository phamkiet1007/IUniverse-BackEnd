package com.iuniverse.controller;

import com.iuniverse.service.EnrollmentService;
import com.iuniverse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "Student Controller", description = "APIs for Students")
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @Operation(summary = "Enroll in a course", description = "Student uses Join Code to enter a course")
    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/course/enroll")
    public ResponseEntity<Object> enrollCourse(@RequestParam String joinCode) {

        Long studentId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        enrollmentService.enrollCourse(joinCode, studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Registration successful! Welcome to the class.");

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}