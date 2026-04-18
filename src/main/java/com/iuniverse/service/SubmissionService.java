package com.iuniverse.service;

import com.iuniverse.common.QuestionType;
import com.iuniverse.controller.request.AnswerRequest;
import com.iuniverse.controller.request.GradeRequest;
import com.iuniverse.controller.request.SubmissionRequest;
import com.iuniverse.controller.response.AnswerDetailResponse;
import com.iuniverse.exception.InvalidDataException;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.*;
import com.iuniverse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SUBMISSION-SERVICE")
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemSetRepository problemSetRepository;
    private final QuestionRepository questionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    @Transactional
    public Long submitAndGrade(Long problemSetId, SubmissionRequest request, Long currentStudentId) {
        log.info("Student {} is submitting Problem Set {}", currentStudentId, problemSetId);

        // 1. Tìm Problem Set
        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem Set does not exist! ID: " + problemSetId + ""));

        // 2. Chốt chặn bảo mật: Sinh viên phải đang ở trong lớp này mới được làm bài!
        Long courseId = problemSet.getModule().getCourse().getId();
        if (!enrollmentRepository.existsByStudentUserIdAndCourseId(currentStudentId, courseId)) {
            throw new AccessDeniedException("You are not enrolled in this course!");
        }

        // 3. Chốt chặn: Không cho nộp 2 lần (Tuỳ requirement, ở đây mình tạm chặn nộp lại)
        if (submissionRepository.existsByStudentUserIdAndProblemSetId(currentStudentId, problemSetId)) {
            throw new InvalidDataException("You have already submitted this Problem Set!");
        }

        // Lấy profile Sinh viên
        Student student = userRepository.findById(currentStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"))
                .getStudentProfile();

        // 4. Tạo vỏ bọc Submission
        Submission submission = Submission.builder()
                .student(student)
                .problemSet(problemSet)
                .totalScore(0.0) // Sẽ cộng dồn ở dưới
                .build();

        double totalEarnedPoints = 0.0;

        // 5. AUTO-GRADING ENGINE (Chấm điểm tự động và Phân loại)
        for (AnswerRequest answerReq : request.getAnswers()) {

            // Lấy câu hỏi từ DB
            Question question = questionRepository.findById(answerReq.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question ID " + answerReq.getQuestionId() + " does not exist!"));

            // Xác minh câu hỏi này có thực sự thuộc về bài tập đang làm không
            if (!question.getProblemSet().getId().equals(problemSetId)) {
                throw new InvalidDataException("Question does not belong to the current Problem Set!");
            }

            boolean isCorrect = false;
            double earnedPoints = 0.0;

            // RẼ NHÁNH THEO LOẠI CÂU HỎI
            if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                // Máy tự động chấm điểm cho Trắc nghiệm
                if (question.getCorrectAns() != null &&
                        question.getCorrectAns().equalsIgnoreCase(answerReq.getStudentResponse().trim())) {
                    isCorrect = true;
                    earnedPoints = question.getPoints(); // Lấy điểm tối đa
                }
            } else if (question.getType() == QuestionType.SHORT_ANSWER || question.getType() == QuestionType.ESSAY) {
                // Đối với Short Answer và Essay: Tạm gán 0 điểm, để false để chờ giảng viên chấm tay
                isCorrect = false;
                earnedPoints = 0.0;
                log.info("Question ID {} is {} type. Assigned 0 points pending manual grading.", question.getId(), question.getType());
            }

            // Cộng vào tổng điểm
            totalEarnedPoints += earnedPoints;

            // Tạo chi tiết câu trả lời
            StudentAnswer studentAnswer = StudentAnswer.builder()
                    .question(question)
                    .studentResponse(answerReq.getStudentResponse().trim())
                    .isCorrect(isCorrect)
                    .earnedPoints(earnedPoints)
                    .build();

            // Gắn vào Submission
            submission.addStudentAnswer(studentAnswer);
        }

        // 6. Cập nhật tổng điểm và Lưu bài nộp
        submission.setTotalScore(totalEarnedPoints);
        return submissionRepository.save(submission).getId();
    }

    @Transactional(readOnly = true)
    public com.iuniverse.controller.response.SubmissionDetailResponse getSubmissionResult(Long submissionId, Long currentStudentId) {

        // 1. Tìm bài nộp
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission does not exist!"));

        // 2. Chốt chặn: Chỉ chủ nhân bài nộp mới được xem
        if (!submission.getStudent().getUser().getId().equals(currentStudentId)) {
            throw new AccessDeniedException("You cannot view this submission!");
        }

        // 3. Map list câu trả lời sang DTO
        List<AnswerDetailResponse> answerDetails = submission.getStudentAnswers().stream()
                .map(ans -> com.iuniverse.controller.response.AnswerDetailResponse.builder()
                        .questionId(ans.getQuestion().getId())
                        .questionContent(ans.getQuestion().getContent())
                        .studentResponse(ans.getStudentResponse())
                        .correctAnswer(ans.getQuestion().getCorrectAns())
                        .isCorrect(ans.getIsCorrect())
                        .earnedPoints(ans.getEarnedPoints())
                        .build())
                .toList();

        // 4. Gom lại và trả về
        return com.iuniverse.controller.response.SubmissionDetailResponse.builder()
                .submissionId(submission.getId())
                .totalScore(submission.getTotalScore())
                .submittedAt(submission.getSubmittedAt())
                .answers(answerDetails)
                .build();
    }

    @Transactional
    public void gradeSubmissionManually(Long submissionId, GradeRequest request, Long teacherId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find submission with ID: " + submissionId + ""));

        // 1. Kiểm tra quyền: Chỉ giảng viên của khóa học này mới được chấm điểm
        if (!submission.getProblemSet().getModule().getCourse().getInstructor().getUser().getId().equals(teacherId)) {
            throw new AccessDeniedException("You are not authorized to grade this submission!");
        }

        // 2. Cập nhật điểm cho từng câu được chỉ định
        for (GradeRequest.AnswerGrade gradeItem : request.getGrades()) {
            StudentAnswer answer = studentAnswerRepository.findById(gradeItem.getStudentAnswerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu trả lời ID: " + gradeItem.getStudentAnswerId()));

            // Chỉ cho phép chấm điểm nếu câu hỏi đó thuộc bài nộp này
            if (!answer.getSubmission().getId().equals(submissionId)) continue;

            answer.setEarnedPoints(gradeItem.getScore());
            // Nếu điểm > 0 thì coi như là đúng (isCorrect = true)
            answer.setIsCorrect(gradeItem.getScore() > 0);
            // give cmt
            answer.setTeacherComment(gradeItem.getTeacherComment());

            studentAnswerRepository.save(answer);
        }

        // 3. TÍNH LẠI TỔNG ĐIỂM (Cực kỳ quan trọng)
        double newTotalScore = submission.getStudentAnswers().stream()
                .mapToDouble(StudentAnswer::getEarnedPoints)
                .sum();

        submission.setTotalScore(newTotalScore);
        submissionRepository.save(submission);

        log.info("Teacher {} updated grades for submission {}. New total score: {}", teacherId, submissionId, newTotalScore);
    }

    // Thêm vào SubmissionService.java

    @Transactional(readOnly = true)
    public List<com.iuniverse.controller.response.SubmissionSummaryResponse> getSubmissionsByProblemSet(Long psId, Long teacherId) {
        // 1. Kiểm tra ProblemSet có tồn tại không
        ProblemSet ps = problemSetRepository.findById(psId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem Set không tồn tại!"));

        // 2. CHỐT CHẶN BẢO MẬT: Kiểm tra xem Teacher có phải là chủ sở hữu khóa học này không
        if (!ps.getModule().getCourse().getInstructor().getUser().getId().equals(teacherId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập danh sách bài nộp của khóa học này!");
        }

        // 3. Lấy tất cả bài nộp của Problem Set này
        List<Submission> submissions = submissionRepository.findAllByProblemSetId(psId);

        // 4. Map sang DTO để trả về
        return submissions.stream()
                .map(s -> com.iuniverse.controller.response.SubmissionSummaryResponse.builder()
                        .submissionId(s.getId())
                        .studentId(s.getStudent().getUser().getId())
                        .studentName(s.getStudent().getUser().getFirstName() + " " + s.getStudent().getUser().getLastName())
                        .studentCode(s.getStudent().getStudentCode())
                        .submittedAt(s.getSubmittedAt())
                        .totalScore(s.getTotalScore())
                        .build())
                .toList();
    }
}