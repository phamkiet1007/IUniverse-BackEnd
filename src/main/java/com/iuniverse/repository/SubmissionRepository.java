package com.iuniverse.repository;

import com.iuniverse.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // Kiểm tra xem sinh viên đã nộp bài này chưa (tránh nộp 2 lần)
    boolean existsByStudentUserIdAndProblemSetId(Long studentId, Long problemSetId);
}