package com.iuniverse.controller;

import com.iuniverse.controller.request.*;
import com.iuniverse.controller.response.CourseResponse;
import com.iuniverse.controller.response.UserResponse;
import com.iuniverse.model.Material;
import com.iuniverse.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@Tag(name = "Course Controller", description = "Managing courses")
@Slf4j(topic = "COURSE-CONTROLLER")
@Validated
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final ModuleService moduleService;
    private final ProblemSetService problemSetService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final FileUploadService fileUploadService;
    private final MaterialService materialService;

    @Operation(summary = "Create new course", description = "Teacher create course & system auto generate Join Code")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PostMapping("/create")
    public ResponseEntity<Object> createCourse(@RequestBody @Valid CourseRequest request) {

        //get username from jwt token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        UserResponse currentUser = userService.findByUsername(currentUsername);
        Long teacherId = currentUser.getId();

        log.info("Teacher {} (ID: {}) is creating new course: {}", currentUsername, teacherId, request.getCourseName());

        Long courseId = courseService.createCourse(request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Create course successfully!");
        result.put("data", courseId);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all my courses ", description = "Teachers view their own courses")
    @PreAuthorize("hasAuthority('TEACHER')")
    @GetMapping("/my-courses")
    public ResponseEntity<Object> getMyCourses() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Long teacherId = userService.findByUsername(currentUsername).getId();

        List<CourseResponse> myCourses = courseService.getMyCourses(teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Get all the courses of this teacher:");
        result.put("data", myCourses);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Teacher view course's details ", description = "Teacher view course's details by ID")
    @PreAuthorize("hasAuthority('TEACHER')")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getCourseDetail(@PathVariable("id") Long courseId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long currentUserId = userService.findByUsername(currentUsername).getId();

        CourseResponse courseDetail = courseService.getCourseDetail(courseId, currentUserId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Success");
        result.put("data", courseDetail);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Add new module to course", description = "Teachers add new module to their courses")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PostMapping("/{courseId}/module")
    public ResponseEntity<Object> createModule(@PathVariable("courseId") Long courseId, @RequestBody @Valid ModuleRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long teacherId = userService.findByUsername(currentUsername).getId();

        Long moduleId = moduleService.createModule(courseId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Create module successfully!");
        result.put("data", moduleId);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Add material to module", description = "Teacher upload link video, pdf into specific modules")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PostMapping("/module/{moduleId}/material")
    public ResponseEntity<Object> addMaterial(
            @PathVariable("moduleId") Long moduleId,
            @RequestBody @Valid MaterialRequest request) {

        //get teacher id from token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long teacherId = userService.findByUsername(currentUsername).getId();

        moduleService.addMaterialToModule(moduleId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Add material successfully!");

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Create problem set (quiz)", description = "Teacher create problem set (quiz)")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PostMapping("/module/{moduleId}/problem-set")
    public ResponseEntity<Object> createProblemSet(
            @PathVariable("moduleId") Long moduleId,
            @RequestBody @Valid ProblemSetRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long teacherId = userService.findByUsername(currentUsername).getId();

        Long problemSetId = problemSetService.createProblemSet(moduleId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Create problem set successfully!");
        result.put("data", problemSetId);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update course", description = "Teacher renames or adjusts the description of the course")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCourse(
            @PathVariable("id") Long courseId,
            @RequestBody @Valid CourseRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long teacherId = userService.findByUsername(authentication.getName()).getId();

        courseService.updateCourse(courseId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Update course successfully!");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Delete course", description = "Teachers delete their courses")
    @PreAuthorize("hasAuthority('TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCourse(@PathVariable("id") Long courseId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long teacherId = userService.findByUsername(authentication.getName()).getId();

        courseService.deleteCourse(courseId, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Delete course successfully!");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // =========================================================================================
    // MODULE APIs (Chương học)
    // =========================================================================================

    @Operation(summary = "Update module", description = "Teacher updates module title or order")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PutMapping("/module/{moduleId}")
    public ResponseEntity<Object> updateModule(
            @PathVariable("moduleId") Long moduleId,
            @RequestBody @Valid ModuleRequest request) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        moduleService.updateModule(moduleId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Update module successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Delete module", description = "Teacher deletes a module")
    @PreAuthorize("hasAuthority('TEACHER')")
    @DeleteMapping("/module/{moduleId}")
    public ResponseEntity<Object> deleteModule(@PathVariable("moduleId") Long moduleId) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        moduleService.deleteModule(moduleId, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Delete module successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // =========================================================================================
    // MATERIAL APIs
    // =========================================================================================

    @Operation(summary = "Update material", description = "Teacher updates material details")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PutMapping("/module/{moduleId}/material/{materialId}")
    public ResponseEntity<Object> updateMaterial(
            @PathVariable("moduleId") Long moduleId,
            @PathVariable("materialId") Long materialId,
            @RequestBody @Valid MaterialRequest request) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        moduleService.updateMaterial(moduleId, materialId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Update material successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Delete material", description = "Teacher removes material from module")
    @PreAuthorize("hasAuthority('TEACHER')")
    @DeleteMapping("/module/{moduleId}/material/{materialId}")
    public ResponseEntity<Object> deleteMaterial(
            @PathVariable("moduleId") Long moduleId,
            @PathVariable("materialId") Long materialId) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        moduleService.deleteMaterial(moduleId, materialId, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Delete material successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // =========================================================================================
    // PROBLEM SET APIs
    // =========================================================================================

    @Operation(summary = "Update problem set", description = "Teacher updates quiz details (without questions)")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PutMapping("/module/problem-set/{problemSetId}")
    public ResponseEntity<Object> updateProblemSet(
            @PathVariable("problemSetId") Long problemSetId,
            @RequestBody @Valid ProblemSetRequest request) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        problemSetService.updateProblemSet(problemSetId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Update problem set successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Delete problem set", description = "Teacher deletes a quiz")
    @PreAuthorize("hasAuthority('TEACHER')")
    @DeleteMapping("/module/problem-set/{problemSetId}")
    public ResponseEntity<Object> deleteProblemSet(@PathVariable("problemSetId") Long problemSetId) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        problemSetService.deleteProblemSet(problemSetId, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Delete problem set successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // =========================================================================================
    // QUESTION APIs
    // =========================================================================================

    @Operation(summary = "Add one question", description = "Teacher adds a single question to an existing Problem Set")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PostMapping("/problem-set/{psId}/question")
    public ResponseEntity<Object> addQuestionToProblemSet(
            @PathVariable("psId") Long psId,
            @RequestBody @Valid QuestionRequest request) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        problemSetService.addSingleQuestion(psId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Add question successfully!");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update question", description = "Teacher updates a specific question")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PutMapping("/question/{questionId}")
    public ResponseEntity<Object> updateQuestion(
            @PathVariable("questionId") Long questionId,
            @RequestBody @Valid QuestionRequest request) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        problemSetService.updateQuestion(questionId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Update question successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Delete question", description = "Teacher deletes a specific question")
    @PreAuthorize("hasAuthority('TEACHER')")
    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<Object> deleteQuestion(@PathVariable("questionId") Long questionId) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        problemSetService.deleteQuestion(questionId, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Delete question successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get course students", description = "Teacher views all students enrolled in their course")
    @PreAuthorize("hasAuthority('TEACHER')")
    @GetMapping("/{id}/students")
    public ResponseEntity<Object> getCourseStudents(@PathVariable("id") Long courseId) {

        Long teacherId = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        List<String> students = enrollmentService.getStudentsInCourse(courseId, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Get all students in this course successfully!");
        result.put("total_students", students.size());
        result.put("data", students);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Grade a student submission", description = "Teacher updates scores for short answers/essays")
    @PreAuthorize("hasAuthority('TEACHER')")
    @PutMapping("/submission/{id}/grade")
    public ResponseEntity<Object> gradeSubmission(
            @PathVariable("id") Long submissionId,
            @RequestBody GradeRequest request,
            Authentication authentication
    ) {
        Long teacherId = userService.findByUsername(authentication.getName()).getId();

        submissionService.gradeSubmissionManually(submissionId, request, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Update grade successfully!");

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get all submissions for a Problem Set", description = "Teacher views list of students who submitted")
    @PreAuthorize("hasAuthority('TEACHER')")
    @GetMapping("/problem-set/{psId}/submissions")
    public ResponseEntity<Object> getSubmissionsByProblemSet(
            @PathVariable Long psId,
            Authentication authentication
    ) {
        Long teacherId = userService.findByUsername(authentication.getName()).getId();

        List<com.iuniverse.controller.response.SubmissionSummaryResponse> data =
                submissionService.getSubmissionsByProblemSet(psId, teacherId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Get all submissions for this problem set successfully!");
        result.put("count", data.size());
        result.put("data", data);

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/upload-material", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Object> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("moduleId") Long moduleId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", defaultValue = "PDF") String type
    ) {
        Material savedMaterial = materialService.uploadAndLinkToModule(file, moduleId, title, type);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Upload và gán tài liệu vào Module thành công!");
        response.put("data", savedMaterial);

        return ResponseEntity.ok(response);
    }
}