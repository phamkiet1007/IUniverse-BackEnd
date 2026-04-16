package com.iuniverse.repository;

import com.iuniverse.model.Course;
import com.iuniverse.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByJoinCode(String joinCode);
    Optional<Course> findByJoinCode(String joinCode);

    List<Course> findAllByInstructor(Teacher instructor);
}