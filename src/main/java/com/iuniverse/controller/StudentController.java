package com.iuniverse.controller;

import com.iuniverse.controller.request.SubmissionRequest;
import com.iuniverse.controller.response.CourseResponse;
import com.iuniverse.model.Course;
import com.iuniverse.model.Rating;
import com.iuniverse.service.EnrollmentService;
import com.iuniverse.service.SubmissionService;
import com.iuniverse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.iuniverse.controller.request.RatingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.iuniverse.service.CourseService;

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
    private final SubmissionService submissionService;
private final CourseService courseService;
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

    @Operation(summary = "Submit Problem Set", description = "Student submits their answers for auto-grading")
    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/problem-set/{psId}/submit")
    public ResponseEntity<Object> submitProblemSet(
            @PathVariable("psId") Long problemSetId,
            @RequestBody @Valid SubmissionRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        Long studentId = userService.findByUsername(username).getId();

        log.info("Student {} is submitting problem set {}", username, problemSetId);

        Long submissionId = submissionService.submitAndGrade(problemSetId, request, studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Submit successfully!");
        result.put("submissionId", submissionId);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "View Submission Result", description = "Student views their graded submission")
    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<Object> getSubmissionResult(
            @PathVariable Long submissionId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        Long studentId = userService.findByUsername(username).getId();

        com.iuniverse.controller.response.SubmissionDetailResponse resultData =
                submissionService.getSubmissionResult(submissionId, studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Get submission result successfully!");
        result.put("data", resultData);

        return ResponseEntity.ok(result);
    }

@PostMapping("/courses/{id}/ratings")
@PreAuthorize("hasAuthority('STUDENT')")
public ResponseEntity<?> rateCourse(
        @PathVariable Long id,
        @RequestBody RatingRequest request,
        Authentication auth) {

    Long studentId = userService.findByUsername(auth.getName()).getId();

    request.setCourseId(id);

    courseService.addRating(studentId, id, request);

    return ResponseEntity.ok("Rated successfully");
}
@GetMapping("/courses/{id}/ratings")
public ResponseEntity<?> getRatings(@PathVariable Long id) {

    List<Rating> ratings = courseService.getRatingsByCourse(id);

    return ResponseEntity.ok(ratings);
}
}