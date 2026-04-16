package com.iuniverse.service;

import com.iuniverse.controller.request.MaterialRequest;
import com.iuniverse.controller.request.ModuleRequest;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Course;
import com.iuniverse.model.Material;
import com.iuniverse.model.Module;
import com.iuniverse.repository.CourseRepository;
import com.iuniverse.repository.MaterialRepository;
import com.iuniverse.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MODULE-SERVICE")
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final MaterialRepository materialRepository;


    @Transactional
    public Long createModule(Long courseId, ModuleRequest req, Long currentTeacherId) {
        log.info("Teacher ID {} is creating a module for Course ID {}", currentTeacherId, courseId);

        //find course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        //check ownership
        if (!course.getInstructor().getUser().getId().equals(currentTeacherId)) {
            log.warn("Teacher {} tried to add a module to Course {} which they don't own.", currentTeacherId, courseId);
            throw new AccessDeniedException("You do not have permission to modify this course.");
        }

        //handle Order Index (auto increase if there is no orderIndex)
        Integer orderIndex = req.getOrderIndex();
        if (orderIndex == null) {
            orderIndex = moduleRepository.findMaxOrderIndexByCourseId(courseId) + 1;
        }

        Module module = Module.builder()
                .course(course)
                .title(req.getTitle())
                .orderIndex(orderIndex)
                .build();

        return moduleRepository.save(module).getId();
    }

    @Transactional
    public void addMaterialToModule(Long moduleId, MaterialRequest req, Long currentTeacherId) {
        log.info("Teacher ID {} is adding material to Module ID {}", currentTeacherId, moduleId);

        //find Module
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        //Module -> Course -> Teacher
        if (!module.getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            log.warn("Teacher {} tried to add material to unauthorized Module {}", currentTeacherId, moduleId);
            throw new AccessDeniedException("You do not have permission to modify this course.");
        }

        Material material = Material.builder()
                .title(req.getTitle())
                .type(req.getType()) // VD: "VIDEO", "PDF"
                .contentUrl(req.getContentUrl())
                .build();

        module.getMaterials().add(material);

        moduleRepository.save(module);
    }

    // --- MODULE LOGIC ---
    @Transactional
    public void updateModule(Long moduleId, ModuleRequest req, Long currentTeacherId) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (!module.getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }
        module.setTitle(req.getTitle());
        if (req.getOrderIndex() != null) module.setOrderIndex(req.getOrderIndex());
        moduleRepository.save(module);
    }

    @Transactional
    public void deleteModule(Long moduleId, Long currentTeacherId) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (!module.getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }
        moduleRepository.delete(module); //
    }

    // --- MATERIAL LOGIC ---
    @Transactional
    public void updateMaterial(Long moduleId, Long materialId, MaterialRequest req, Long currentTeacherId) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (!module.getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }

        Material material = materialRepository.findById(materialId).orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        material.setTitle(req.getTitle());
        material.setType(req.getType());
        material.setContentUrl(req.getContentUrl());
        materialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long moduleId, Long materialId, Long currentTeacherId) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (!module.getCourse().getInstructor().getUser().getId().equals(currentTeacherId)) {
            throw new AccessDeniedException("Access denied!");
        }

        Material material = materialRepository.findById(materialId).orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        // Vì là quan hệ Many-To-Many, ta gỡ Material ra khỏi Module thay vì xóa thẳng Material
        module.getMaterials().remove(material);
        moduleRepository.save(module);

        //Nếu Material không còn nằm trong Module nào khác, có thể xóa hẳn nó khỏi DB
        // materialRepository.delete(material);
    }
}