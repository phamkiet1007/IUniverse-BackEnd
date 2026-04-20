package com.iuniverse.repository;

import com.iuniverse.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    boolean existsByIdAndProblemSetId(Long id, Long problemSetId);
     List<Question> findByProblemSetId(Long problemSetId);
}