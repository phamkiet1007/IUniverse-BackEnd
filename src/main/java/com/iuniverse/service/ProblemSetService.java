package com.iuniverse.service;

import com.iuniverse.common.QuestionType;
import com.iuniverse.controller.request.ProblemSetRequest;
import com.iuniverse.controller.request.QuestionRequest;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Module;
import com.iuniverse.model.ProblemSet;
import com.iuniverse.model.Question;
import com.iuniverse.repository.ModuleRepository;
import com.iuniverse.repository.ProblemSetRepository;
import com.iuniverse.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PROBLEM-SET-SERVICE")
public class ProblemSetService {

    private final ProblemSetRepository problemSetRepository;
    private final ModuleRepository moduleRepository;
    private final QuestionRepository questionRepository;

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
}