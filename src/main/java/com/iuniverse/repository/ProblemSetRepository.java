package com.iuniverse.repository;

import com.iuniverse.model.ProblemSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {
List<ProblemSet> findByModuleId(Long moduleId);
}
