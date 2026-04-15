package com.iuniverse.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import com.iuniverse.service.EnrollmentService;
import com.iuniverse.controller.request.EnrollmentRequest;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentCourseController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<String> enroll(@RequestBody EnrollmentRequest request) {
        // Tạm thời fix cứng studentId = 1 để test logic. 
        // Khi nào có SecurityUtils thì thay lại: Long currentStudentId = SecurityUtils.getCurrentUserId();
        Long currentStudentId = 1L; 
        
        enrollmentService.enrollByCode(request.getJoinCode(), currentStudentId);
        
        return ResponseEntity.ok("Ghi danh thành công!");
    }
}