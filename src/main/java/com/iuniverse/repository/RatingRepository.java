package com.iuniverse.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.iuniverse.model.Rating;
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByCourseId(Long courseId);
}
