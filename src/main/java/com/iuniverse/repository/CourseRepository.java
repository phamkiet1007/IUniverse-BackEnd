package com.iuniverse.repository;
import java.util.Optional;
import com.iuniverse.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByJoinCode(String joinCode);
}