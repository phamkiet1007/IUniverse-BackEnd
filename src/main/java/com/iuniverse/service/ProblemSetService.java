package com.iuniverse.service;

import com.iuniverse.common.QuestionType;
import com.iuniverse.controller.request.ProblemSetRequest;
import com.iuniverse.controller.request.QuestionRequest;
import com.iuniverse.controller.response.ProblemSetResponse;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Module;
import com.iuniverse.model.ProblemSet;
import com.iuniverse.model.Question;
import com.iuniverse.repository.EnrollmentRepository;
import com.iuniverse.repository.ModuleRepository;
import com.iuniverse.repository.ProblemSetRepository;
import com.iuniverse.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PROBLEM-SET-SERVICE")
public class ProblemSetService {

    private final ProblemSetRepository problemSetRepository;
    private final ModuleRepository moduleRepository;
    private final QuestionRepository questionRepository;
    private final EnrollmentRepository enrollmentRepository;


    @Transactional
    public Long createProblemSet(Long moduleId, ProblemSetRequest req, Long currentTeacherId) {
        log.info("Teacher {} is creating exercises for Module {}", currentTeacherId, moduleId);

        //find Module
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        if (!module.getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied! You are not authorized.");
        }

        ProblemSet problemSet = ProblemSet.builder()
                .module(module)
                .title(req.getTitle())
                .description(req.getDescription())
                .dueDate(req.getDueDate())
                .timeLimitMins(req.getTimeLimitMins())
                .build();

        if (req.getQuestions() != null && !req.getQuestions().isEmpty()) {
            for (QuestionRequest qReq : req.getQuestions()) {
                Question question = Question.builder()
                        .content(qReq.getContent())
                        .type(QuestionType.valueOf(qReq.getType().trim().toUpperCase()))
                        .correctAns(qReq.getCorrectAns())
                        .points(qReq.getPoints())
                        .options(qReq.getOptions())
                        .build();

                problemSet.addQuestion(question);
            }
        }

        return problemSetRepository.save(problemSet).getId();
    }


    // --- PROBLEM SET LOGIC ---
    @Transactional
    public void updateProblemSet(Long problemSetId, ProblemSetRequest req, Long currentTeacherId) {
        ProblemSet ps = problemSetRepository.findById(problemSetId).orElseThrow(() -> new ResourceNotFoundException("Problem Set not found"));
        if (!ps.getModule().getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }
        ps.setTitle(req.getTitle());
        ps.setDescription(req.getDescription());
        ps.setDueDate(req.getDueDate());
        ps.setTimeLimitMins(req.getTimeLimitMins());
        problemSetRepository.save(ps);
    }

    @Transactional
    public void deleteProblemSet(Long problemSetId, Long currentTeacherId) {
        ProblemSet ps = problemSetRepository.findById(problemSetId).orElseThrow(() -> new ResourceNotFoundException("Problem Set not found"));
        if (!ps.getModule().getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }
        problemSetRepository.delete(ps); // Cascade tự động xóa hết Question và Option
    }

    // --- QUESTION LOGIC ---
    @Transactional
    public void addSingleQuestion(Long psId, QuestionRequest req, Long currentTeacherId) {
        ProblemSet ps = problemSetRepository.findById(psId).orElseThrow(() -> new ResourceNotFoundException("Problem Set not found"));
        if (!ps.getModule().getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }

        Question question = Question.builder()
                .content(req.getContent())
                .type(QuestionType.valueOf(req.getType().trim().toUpperCase()))
                .correctAns(req.getCorrectAns())
                .points(req.getPoints())
                .options(req.getOptions())
                .build();

        ps.addQuestion(question);
        problemSetRepository.save(ps);
    }

    @Transactional
    public void updateQuestion(Long questionId, QuestionRequest req, Long currentTeacherId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Đi ngược lên ProblemSet -> Module -> Course để check quyền
        if (!question.getProblemSet().getModule().getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }

        question.setContent(req.getContent());
        question.setType(QuestionType.valueOf(req.getType().trim().toUpperCase()));
        question.setCorrectAns(req.getCorrectAns());
        question.setPoints(req.getPoints());
        question.setOptions(req.getOptions());
        questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long currentTeacherId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (!question.getProblemSet().getModule().getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }

        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public List<ProblemSetResponse> getProblemSetsByModule(Long moduleId, Long teacherId) {

        // 1. Tìm Module
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // 2. Check quyền: Giáo viên này có phải chủ khóa học không?
        if (!module.getCourse().getInstructor().getUser().getId().equals(teacherId)) {
            throw new AccessDeniedException("Access denied!");
        }

        // 3. Map từ Entity sang Response
        return module.getProblemSets().stream().map(ps ->
                ProblemSetResponse.builder()
                        .id(ps.getId())
                        .title(ps.getTitle())
                        .description(ps.getDescription())
                        .dueDate(ps.getDueDate()) // Lấy từ DB
                        .timeLimitMins(ps.getTimeLimitMins()) // Lấy từ DB
                        .build()
        ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProblemSetResponse> getProblemSetsByModuleForStudent(Long moduleId, Long studentId) {

        // 1. Tìm Module
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // 2. BẢO MẬT DÀNH CHO STUDENT: Kiểm tra xem sinh viên có đang học khóa này không
        // Chỗ này bạn gọi repository check enrollment của bạn nhé (ví dụ minh họa)
        boolean isEnrolled = enrollmentRepository.existsByCourseIdAndStudent_UserId(module.getCourse().getId(), studentId);
        if (!isEnrolled) {
            log.warn("Student {} tried to access Problem Sets of un-enrolled Course {}", studentId, module.getCourse().getId());
            throw new AccessDeniedException("You are not enrolled in this course!");
        }

        // 3. Map từ Entity sang Response (Giống hệt ở trên)
        return module.getProblemSets().stream().map(ps ->
                ProblemSetResponse.builder()
                        .id(ps.getId())
                        .title(ps.getTitle())
                        .description(ps.getDescription())
                        .dueDate(ps.getDueDate())
                        .timeLimitMins(ps.getTimeLimitMins())
                        .build()
        ).collect(Collectors.toList());
    }
}