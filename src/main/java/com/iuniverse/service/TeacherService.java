package com.iuniverse.service;

import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Teacher;
import com.iuniverse.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TEACHER-SERVICE")
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public Teacher getTeacherEntityById(Long id) {
        log.info("Checking Teacher privileges for User ID: {}", id);

        return teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User ID {} is not a Teacher. Access denied.", id);
                    return new AccessDeniedException("Access denied!");
                });
    }
}