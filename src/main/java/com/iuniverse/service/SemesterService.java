package com.iuniverse.service;

import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Semester;
import com.iuniverse.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;
    public Semester getSemesterEntityById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));
    }
}
