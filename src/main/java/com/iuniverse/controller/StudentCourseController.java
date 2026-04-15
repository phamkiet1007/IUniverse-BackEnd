// controller/StudentCourseController.java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import com.iuniverse.service.EnrollmentService;
import com.iuniverse.controller.request.EnrollmentRequest;
// Nếu bạn có class SecurityUtils thì import vào, không thì tạm thời comment dòng đó lại
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentCourseController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<String> enroll(@RequestBody EnrollmentRequest request) {
        // Giả sử bạn lấy studentId từ Token, ở đây mình ví dụ truyền cứng hoặc lấy từ Context
        Long currentStudentId = SecurityUtils.getCurrentUserId(); 
        
        enrollmentService.enrollByCode(request.getJoinCode(), currentStudentId);
        
        return ResponseEntity.ok("Ghi danh thành công!");
    }
}