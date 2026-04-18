package com.iuniverse.service;

import com.iuniverse.model.Course;

import java.util.List;

public interface EnrollmentService {

    void enrollByCode(String joinCode, Long studentId);

    List<Course> getCoursesByStudent(Long studentId);

    List<String> getStudentsInCourse(Long courseId, Long teacherId);
}